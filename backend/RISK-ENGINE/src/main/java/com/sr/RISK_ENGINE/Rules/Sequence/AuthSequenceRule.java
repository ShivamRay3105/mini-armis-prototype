package com.sr.RISK_ENGINE.Rules.Sequence;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;
import com.sr.RISK_ENGINE.util.EventUtils;

public class AuthSequenceRule implements RiskRule {

    @Override
    public double evaluate(AssetContext context) {

        EventUtils eventUtils = new EventUtils(context);

        Deque<AssetEvent> events = context.getRecentEvents();

        double score = 0.0;

        boolean hasAuthFailure = eventUtils.hasSequence(events, EventType.AUTH_FAILURE, EventType.UNAUTHORIZED_ACCESS,
                120);

        if (hasAuthFailure) {
            score = Math.max(score, 40.0);
        }

        boolean hasAuthSuccess = eventUtils.hasSequence(events, EventType.AUTH_SUCCESS, EventType.UNAUTHORIZED_ACCESS,
                120);
        if (hasAuthSuccess) {
            score = Math.max(score, 50.0);
        }

        return score;
    }

}
