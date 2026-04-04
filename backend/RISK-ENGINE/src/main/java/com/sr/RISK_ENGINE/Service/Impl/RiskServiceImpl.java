package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.Enums.RiskLevel;
import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.Rules.Base.BaseScoreRule;
import com.sr.RISK_ENGINE.Rules.Frequency.AuthFailureFrequencyRule;
import com.sr.RISK_ENGINE.Rules.Frequency.PortScanFrequencyRule;
import com.sr.RISK_ENGINE.Rules.Sequence.AuthSequenceRule;
import com.sr.RISK_ENGINE.Rules.Sequence.PrivilegeEscalationRule;
import com.sr.RISK_ENGINE.Service.IdempotencyService;
import com.sr.RISK_ENGINE.Service.ResilientAssetClient;
import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.dto.UpdateRiskRequest;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.RiskEvent;
import com.sr.RISK_ENGINE.producer.RiskEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final RedisTemplate<String, AssetContext> assetTemplate;

    private final IdempotencyService idempotencyService;

    private final ResilientAssetClient resilientAssetClient;

    private final RiskEventProducer riskEventProducer;

    // Rule list
    private final List<RiskRule> rules = List.of(
            new BaseScoreRule(),
            new AuthFailureFrequencyRule(),
            new AuthSequenceRule(),
            new PrivilegeEscalationRule(),
            new PortScanFrequencyRule());

    @Override
    public void processEvent(AssetEvent event) {

        try {
            log.info("Processing event: {} for asset: {}", event.eventId(), event.assetId());
            // 1. Idempotency Check
            boolean isDuplicate = false;
            try {
                isDuplicate = idempotencyService.isDuplicate(event.eventId());
            } catch (Exception e) {
                log.warn("Idempotency check failed (Redis down). Proceeding with processing to avoid data loss.");
            }
            if (isDuplicate) {
                log.warn("Duplicate Event Ignored:");
                return;
            }

            // Get or create context // Context Retrival (Redis)
            String key = "asset:" + event.assetId();
            AssetContext assetContext = null;
            try {
                assetContext = assetTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.error("Failed to retrieve context from Redis: {}. Creating temporary context.", e.getMessage());
            }
            if (assetContext == null) {
                assetContext = new AssetContext(event.assetId());
            }

            // Update state
            assetContext.addEvent(event);
            log.info("Adding the event to the context, current number of events in context: {}",
                    assetContext.getRecentEvents().size());
            assetContext.removeOldEvents();
            log.info("Removing old events, current number of events in context: {}",
                    assetContext.getRecentEvents().size());

            // old score
            double oldScore = assetContext.getRiskScore();

            // Calculate risk
            log.info("Calculating risk score for asset {}", event.assetId());
            double newScore = calculateRisk(assetContext);
            log.info("Risk score Calculated");

            // Apply decay (optional in same step)
            log.info("Risk Score updated locally for asset {}", event.assetId());
            assetContext.updateRisk(newScore, event.timestamp());

            // SAVE BACK TO REDIS
            log.info("Saving updated context back to Redis for asset {}", event.assetId());
            try {
                assetTemplate.opsForValue().set(key, assetContext, Duration.ofMinutes(45));
            } catch (Exception e) {
                log.error("Failed to save context to Redis for asset {}: {}", event.assetId(), e.getMessage());
                // Note: If this fails, the next event will start from a fresh context.
            }

            // Push to Asset Service (only if needed)
            log.info("Checking if asset service update is needed for asset {}", event.assetId());
            maybeUpdateAssetService(assetContext, oldScore, event);

        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.eventId(), e.getMessage());
        } finally {
            log.info("Finished processing event: {} for asset: {}", event.eventId(), event.assetId());
        }
    }

    private double calculateRisk(AssetContext assetContext) {
        // 1. Reset current rule score to 0.0
        double currentRuleScore = 0.0;

        log.info("Applying rules for asset {}", assetContext.getAssetId());
        for (RiskRule rule : rules) {
            currentRuleScore += rule.evaluate(assetContext);
        }
        log.info("Rules applied for asset {}, current rule score: {}", assetContext.getAssetId(), currentRuleScore);

        // 2. Calculate what the PREVIOUS score would be after decay
        log.info("Calculating potential decay for asset {}", assetContext.getAssetId());
        double decayedScore = applyDecay(assetContext.getRiskScore(), assetContext);

        // 3. THE MASTER LOGIC: Use whichever is higher.
        // If the rules are spiking (attack), rules win.
        // If the rules are 0 (no attack), the decayed historical score wins.
        double finalScore = Math.max(currentRuleScore, decayedScore);

        // 4. Cap at 100 and 0
        finalScore = Math.min(Math.max(finalScore, 0.0), 100.0);

        log.info("Asset {}: Rule Score: {}, Decayed Score: {}, Final Output: {}",
                assetContext.getAssetId(), currentRuleScore, decayedScore, finalScore);

        return finalScore;
    }

    private double applyDecay(double score, AssetContext assetContext) {

        // we are using process time (Instant.now()) instead of event time for decay
        // calculation, this is a simplification and can be improved by using the
        // timestamp of the latest event in the context

        // 1. Check if we actually have a previous update to decay from
        if (assetContext.getLastUpdated() == null || assetContext.getRecentEvents().isEmpty()) {
            log.info("First event for asset or no previous state. Skipping decay.");
            return score;
        }
        if (assetContext.isRequiresManualReset()) {
            // If locked, it cannot decay below 80.0
            return Math.max(score, 80.0);
        }
        // 3. Calculate decay based on the gap between the LAST update and THIS event
        long seconds = Duration.between(
                assetContext.getLastUpdated(),
                Instant.now()).toSeconds();
        double decay = seconds * 0.1;
        log.info("Decaying score by {} based on {} seconds of inactivity", decay, seconds);
        score -= decay;

        return Math.max(score, 0);
    }

    public void maybeUpdateAssetService(AssetContext assetContext, double oldScore, AssetEvent currentEvent) {

        double newRiskScore = assetContext.getRiskScore();

        double delta = Math.abs(newRiskScore - oldScore);

        // Always update if the score has finally hit zero
        boolean isFinalZero = (newRiskScore == 0 && oldScore > 0);

        // Check for "Atomic" (Immediate) Alert triggers
        boolean isAtomicAlert = false;
        if (currentEvent != null) {
            isAtomicAlert = List.of(
                    EventType.UNAUTHORIZED_ACCESS,
                    EventType.PRIVILEGE_ESCALATION).contains(currentEvent.eventType());
        }
        // Activate the Sticky Floor if score hits Critical
        if (newRiskScore >= 80 && !assetContext.isRequiresManualReset()) {
            log.warn("STICKY FLOOR ACTIVATED: Asset {} is now locked at Critical risk.", assetContext.getAssetId());
            assetContext.setRequiresManualReset(true);
        }

        if (delta >= 10 || riskLevelChanged(oldScore, newRiskScore) || isAtomicAlert || isFinalZero) {
            log.info(
                    "Risk level change detected for asset {}. Old Score: {}, New Score: {}. Preparing to update asset service.",
                    assetContext.getAssetId(), oldScore, newRiskScore);
            UpdateRiskRequest request = new UpdateRiskRequest(assetContext.getAssetId(), newRiskScore);

            // CALL THE BRIDGE: This triggers the Retry and Circuit Breaker logic
            log.info("Sending update to asset service for asset {} with new risk score {}", assetContext.getAssetId(),
                    newRiskScore);
            resilientAssetClient.sendUpdate(request);

            // If newRiskScore is dangerous, send a RiskEvent to Kafka
            if (currentEvent != null && (newRiskScore >= 70 || isAtomicAlert)) {
                String reason = isAtomicAlert ? "CRITICAL EVENT: " + currentEvent.eventType()
                        : "Risk Threshold Exceeded";
                log.info("Risk score {} for asset {} exceeds threshold. Sending alert. Reason: {}", newRiskScore,
                        assetContext.getAssetId(), reason);
                RiskEvent riskEvent = new RiskEvent(
                        UUID.randomUUID(),
                        assetContext.getAssetId(),
                        newRiskScore,
                        getRiskLevel(newRiskScore).toString(),
                        reason,
                        Instant.now());
                riskEventProducer.sendRiskAlert(riskEvent);
            }
        }
    }

    private RiskLevel getRiskLevel(double score) {
        if (score >= 80)
            return RiskLevel.CRITICAL;
        if (score >= 50)
            return RiskLevel.HIGH;
        if (score >= 20)
            return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private boolean riskLevelChanged(double oldScore, double newScore) {
        // helper method
        return getRiskLevel(oldScore) != getRiskLevel(newScore);
    }

    @Scheduled(fixedRate = 10000) // every 10 seconds
    public void performGlobalDecay() {
        log.info("--- STARTING SCHEDULED GLOBAL DECAY ---");

        // 1. Get all keys from Redis starting with "asset:"
        Set<String> keys = assetTemplate.keys("asset:*");

        if (keys == null || keys.isEmpty()) {
            log.info("No active assets found in Redis for decay.");
            return;
        }
        for (String key : keys) {
            try {
                AssetContext assetContext = assetTemplate.opsForValue().get(key);

                if (assetContext == null)
                    continue;

                // If the score is already 0, we don't need to decay it further
                if (assetContext.getRiskScore() <= 0)
                    continue;

                double oldScore = assetContext.getRiskScore();

                // 2. Re-calculate risk (this calls applyDecay inside)
                double newScore = calculateRisk(assetContext);

                // 3. Only update if the score actually changed
                if (newScore != oldScore) {
                    log.info("Decaying Asset {}: {} -> {}", assetContext.getAssetId(), oldScore, newScore);
                    assetContext.updateRisk(newScore, Instant.now());

                    // Save the updated state back to Redis
                    assetTemplate.opsForValue().set(key, assetContext, java.time.Duration.ofMinutes(45));

                    // 4. Update the Asset Service and Alert Service if needed
                    maybeUpdateAssetService(assetContext, oldScore, null);
                }
            } catch (Exception e) {
                log.error("Error during scheduled decay for key {}: {}", key, e.getMessage());
            }
        }
        log.info("--- GLOBAL DECAY FINISHED ---");
    }
}
