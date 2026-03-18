package com.sr.RISK_ENGINE.Service.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;
import com.sr.RISK_ENGINE.model.RiskLevel;

@Service
public class RiskServiceImpl implements RiskService {

    private final Map<Long, AssetContext> stateStore = new ConcurrentHashMap<>();

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

        Deque<AssetEvent> events = assetContext.getRecentEvents();

        double score = 0;

        // base score
        score += calculateBaseScore(events);

        // frequency and patterns
        score += detectFrequency(events);

        // sequences
        score += detectSequences(events);

        // (later) decay rules
        score = applyDecay(score, assetContext);

        score = Math.min(score, 100);

        return score;
    }

    private double getWeight(EventType type) {
        return switch (type) {
            case AUTH_FAILURE -> 5.0;
            case UNAUTHORIZED_ACCESS -> 20.0;
            default -> 0.0;
        };
    }

    private double calculateBaseScore(Deque<AssetEvent> events) {
        double baseScore = 0.0;
        for (AssetEvent event : events) {
            baseScore += getWeight(event.eventType());
        }
        return baseScore;
    }

    private double detectFrequency(Deque<AssetEvent> events) {
        int authFailures = 0;
        for (AssetEvent event : events) {
            if (event.eventType() == EventType.AUTH_FAILURE) {
                authFailures++;
            }
        }
        if (authFailures >= 10)
            return 50.0;
        else if (authFailures >= 5)
            return 25.0;
        else if (authFailures >= 3)
            return 10.0;
        return 0.0;
    }

    private double detectSequences(Deque<AssetEvent> events) {

        double sequenceScore = 0.0;

        List<AssetEvent> eventList = new ArrayList<>(events);

        for (int i = 0; i < eventList.size(); i++) {

            if (eventList.get(i).eventType() == EventType.UNAUTHORIZED_ACCESS) {

                // look backward
                for (int j = i - 1; j >= 0; j--) {

                    if (sequenceScore == 50.0)
                        break; // max score for this sequence of auth events

                    long timeDifference = Duration.between(eventList.get(j).timestamp(), eventList.get(i).timestamp())
                            .toSeconds();
                    if (timeDifference > 120)
                        break;

                    if (eventList.get(j).eventType() == EventType.AUTH_FAILURE) {
                        sequenceScore = Math.max(sequenceScore, 40.0);
                    } else if (eventList.get(j).eventType() == EventType.AUTH_SUCCESS) {
                        sequenceScore = Math.max(sequenceScore, 50.0);
                    }
                }
            }
        }
        return sequenceScore;
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
