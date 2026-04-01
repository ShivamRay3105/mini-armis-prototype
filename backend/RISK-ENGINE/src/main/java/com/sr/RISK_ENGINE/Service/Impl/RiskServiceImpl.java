package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
import com.sr.RISK_ENGINE.model.RiskLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final RedisTemplate<String, AssetContext> assetTemplate;

    private final IdempotencyService idempotencyService;

    private final ResilientAssetClient resilientAssetClient;

    // Rule list
    private final List<RiskRule> rules = List.of(
            new BaseScoreRule(),
            new AuthFailureFrequencyRule(),
            new AuthSequenceRule(),
            new PrivilegeEscalationRule(),
            new PortScanFrequencyRule());

    @Override
    public void processEvent(AssetEvent event) {

        if (idempotencyService.isDuplicate(event.eventId())) {
            System.out.println("Duplicate Event Ignored: " + event.eventId());
            return;
        }

        // Get or create context
        String key = "asset:" + event.assetId();
        AssetContext assetContext = assetTemplate.opsForValue().get(key);

        if (assetContext == null) {
            assetContext = new AssetContext(event.assetId());
        }

        // Update state
        assetContext.addEvent(event);
        assetContext.removeOldEvents();

        // old score
        double oldScore = assetContext.getRiskScore();

        // Calculate risk
        double newScore = calculateRisk(assetContext);

        // Apply decay (optional in same step)
        assetContext.updateRisk(newScore, event.timestamp());

        // SAVE BACK TO REDIS
        assetTemplate.opsForValue().set(key, assetContext, Duration.ofMinutes(15));

        // Push to Asset Service (only if needed)
        maybeUpdateAssetService(assetContext, oldScore);
    }

    private double calculateRisk(AssetContext assetContext) {

        double score = 0;

        for (RiskRule rule : rules) {
            score += rule.evaluate(assetContext);
        }
        // (later) decay rules
        score = applyDecay(score, assetContext);

        score = Math.min(score, 100);

        return score;
    }

    private double applyDecay(double score, AssetContext assetContext) {

        // we are using process time (Instant.now()) instead of event time for decay
        // calculation, this is a simplification and can be improved by using the
        // timestamp of the latest event in the context

        Instant latestEventTime = assetContext.getRecentEvents().peekLast().timestamp();
        long seconds = Duration.between(
                assetContext.getLastUpdated(),
                latestEventTime).toSeconds();
        double decay = seconds * 0.1;
        score -= decay;

        return Math.max(score, 0);
    }

    public void maybeUpdateAssetService(AssetContext assetContext, double oldScore) {
        double newRiskScore = assetContext.getRiskScore();
        double delta = Math.abs(newRiskScore - oldScore);

        if (delta >= 10 || riskLevelChanged(oldScore, newRiskScore)) {
            UpdateRiskRequest request = new UpdateRiskRequest(assetContext.getAssetId(), newRiskScore);

            // CALL THE BRIDGE: This triggers the Retry and Circuit Breaker logic
            resilientAssetClient.sendUpdate(request);
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
