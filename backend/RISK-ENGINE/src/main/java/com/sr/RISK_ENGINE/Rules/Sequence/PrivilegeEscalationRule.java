package com.sr.RISK_ENGINE.Rules.Sequence;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;
import com.sr.RISK_ENGINE.util.EventUtils;

public class PrivilegeEscalationRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {

        EventUtils eventUtils = new EventUtils(context);

        Deque<AssetEvent> events = context.getRecentEvents();

        double finalScore = 0.0;

        boolean hasUnauthorizedAccess = eventUtils.hasSequence(events, EventType.UNAUTHORIZED_ACCESS,
                EventType.PRIVILEGE_ESCALATION, 120);

        if (hasUnauthorizedAccess) {
            finalScore = Math.max(finalScore, 70.0);
        }

        boolean hasAuthSuccess = eventUtils.hasSequence(events, EventType.AUTH_SUCCESS, EventType.PRIVILEGE_ESCALATION,
                120);
        if (hasAuthSuccess) {
            finalScore = Math.max(finalScore, 40.0);
        }

        return finalScore;
    }
}
