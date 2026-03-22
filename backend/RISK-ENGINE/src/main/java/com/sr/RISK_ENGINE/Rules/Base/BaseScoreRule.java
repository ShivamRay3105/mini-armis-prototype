package com.sr.RISK_ENGINE.Rules.Base;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;

public class BaseScoreRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {
        Deque<AssetEvent> events = context.getRecentEvents();

        double score = 0.0;
        for (AssetEvent event : events) {
            score += getWeight(event.eventType());
        }
        return score;
    }

    private double getWeight(EventType type) {
        return switch (type) {
            case AUTH_FAILURE -> 5.0;
            case UNAUTHORIZED_ACCESS -> 20.0;
            case PRIVILEGE_ESCALATION -> 5.0;
            default -> 0.0;
        };
    }
}
