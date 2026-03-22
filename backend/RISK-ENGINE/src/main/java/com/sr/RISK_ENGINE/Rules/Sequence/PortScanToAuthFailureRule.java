package com.sr.RISK_ENGINE.Rules.Sequence;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;
import com.sr.RISK_ENGINE.util.EventUtils;

public class PortScanToAuthFailureRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {

        EventUtils eventUtils = new EventUtils(context);

        Deque<AssetEvent> events = context.getRecentEvents();

        double finalScore = 0.0;

        boolean mediumPortScans = eventUtils.hasSequence(events, EventType.PORT_SCAN, EventType.AUTH_FAILURE, 60);
        if (mediumPortScans) {
            finalScore = Math.max(finalScore, 20.0);
        }

        boolean heavyPortScans = eventUtils.hasSequence(events, EventType.PORT_SCAN, EventType.AUTH_FAILURE, 30);
        if (heavyPortScans) {
            finalScore = Math.max(finalScore, 30.0);
        }

        return finalScore;
    }
}
