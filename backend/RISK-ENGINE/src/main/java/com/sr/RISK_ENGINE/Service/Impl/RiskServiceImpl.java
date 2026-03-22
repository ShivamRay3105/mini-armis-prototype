package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.Rules.Base.BaseScoreRule;
import com.sr.RISK_ENGINE.Rules.Frequency.AuthFailureFrequencyRule;
import com.sr.RISK_ENGINE.Rules.Frequency.PortScanFrequencyRule;
import com.sr.RISK_ENGINE.Rules.Sequence.AuthSequenceRule;
import com.sr.RISK_ENGINE.Rules.Sequence.PrivilegeEscalationRule;
import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.RiskLevel;

@Service
public class RiskServiceImpl implements RiskService {

    private final Map<Long, AssetContext> stateStore = new ConcurrentHashMap<>();

    // Rule list
    private final List<RiskRule> rules = List.of(
            new BaseScoreRule(),
            new AuthFailureFrequencyRule(),
            new AuthSequenceRule(),
            new PrivilegeEscalationRule(),
            new PortScanFrequencyRule());

    @Override
    public void processEvent(AssetEvent event) {

        // Get or create context
        AssetContext assetContext = stateStore.computeIfAbsent(
                event.assetId(),
                AssetContext::new);

        // Update state
        assetContext.addEvent(event);
        assetContext.removeOldEvents(Instant.now().minusSeconds(300));

        // old score
        double oldScore = assetContext.getRiskScore();

        // Calculate risk
        double newScore = calculateRisk(assetContext);

        // Apply decay (optional in same step)
        assetContext.updateRisk(newScore);

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

        long seconds = Duration.between(
                assetContext.getLastUpdated(),
                Instant.now()).toSeconds();
        double decay = seconds * 0.1;
        score -= decay;

        return Math.max(score, 0);
    }

    private void maybeUpdateAssetService(AssetContext assetContext, double oldScore) {
        // we will be using 2 main conditions, delta change ( change in old timestamp
        // and new timestamp, here right now i am using process time instead of event
        // time)
        // and second is risk tier change ( low, medium, high, critical) based on
        // thresholds

        double newRiskScore = assetContext.getRiskScore();

        double delta = Math.abs(newRiskScore - oldScore);
        if (delta >= 10 || riskLevelChanged(oldScore, newRiskScore)) {

            // call asset service to update risk score
            // assetService.updateRiskScore(assetContext.getAssetId(), newScore);
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
