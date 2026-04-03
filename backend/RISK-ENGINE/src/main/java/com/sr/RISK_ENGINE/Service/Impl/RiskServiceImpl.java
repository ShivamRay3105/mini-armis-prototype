package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
                assetTemplate.opsForValue().set(key, assetContext, Duration.ofMinutes(15));
            } catch (Exception e) {
                log.error("Failed to save context to Redis for asset {}: {}", event.assetId(), e.getMessage());
                // Note: If this fails, the next event will start from a fresh context.
            }

            // Push to Asset Service (only if needed)
            log.info("Checking if asset service update is needed for asset {}", event.assetId());
            maybeUpdateAssetService(assetContext, oldScore);

        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.eventId(), e.getMessage());
        } finally {
            log.info("Finished processing event: {} for asset: {}", event.eventId(), event.assetId());
        }
    }

    private double calculateRisk(AssetContext assetContext) {

        double score = 0;

        log.info("Applying rules for asset {}", assetContext.getAssetId());
        for (RiskRule rule : rules) {
            score += rule.evaluate(assetContext);
        }
        log.info("Rules applied for asset {}, intermediate score: {}", assetContext.getAssetId(), score);

        // (later) decay rules
        log.info("Applying decay for asset {}", assetContext.getAssetId());
        score = applyDecay(score, assetContext);
        log.info("Decay applied for asset {}, final score: {}", assetContext.getAssetId(), score);

        score = Math.min(score, 100);
        log.info("Final risk score after calculating the Risk for asset {}: {}", assetContext.getAssetId(), score);
        return score;
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

        AssetEvent lastEvent = assetContext.getRecentEvents().peekLast();
        if (lastEvent == null)
            return score;
        Instant latestEventTime = lastEvent.timestamp();

        // 3. Calculate decay based on the gap between the LAST update and THIS event
        long seconds = Duration.between(
                assetContext.getLastUpdated(),
                latestEventTime).toSeconds();
        double decay = seconds * 0.1;
        log.info("Decaying score by {} based on {} seconds of inactivity", decay, seconds);
        score -= decay;

        return Math.max(score, 0);
    }

    public void maybeUpdateAssetService(AssetContext assetContext, double oldScore) {
        double newRiskScore = assetContext.getRiskScore();
        double delta = Math.abs(newRiskScore - oldScore);

        if (delta >= 10 || riskLevelChanged(oldScore, newRiskScore)) {
            log.info(
                    "Risk level change detected for asset {}. Old Score: {}, New Score: {}. Preparing to update asset service.",
                    assetContext.getAssetId(), oldScore, newRiskScore);
            UpdateRiskRequest request = new UpdateRiskRequest(assetContext.getAssetId(), newRiskScore);

            // CALL THE BRIDGE: This triggers the Retry and Circuit Breaker logic
            log.info("Sending update to asset service for asset {} with new risk score {}", assetContext.getAssetId(),
                    newRiskScore);
            resilientAssetClient.sendUpdate(request);

            // If newRiskScore is dangerous, send a RiskEvent to Kafka
            if (newRiskScore >= 70) {
                RiskEvent riskEvent = new RiskEvent(
                        UUID.randomUUID(),
                        assetContext.getAssetId(),
                        newRiskScore,
                        getRiskLevel(newRiskScore).toString(),
                        "Anomalous behavior threshold exceeded. Manual audit required.",
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
}
