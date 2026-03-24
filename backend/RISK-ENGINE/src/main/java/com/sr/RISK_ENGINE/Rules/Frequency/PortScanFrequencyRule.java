package com.sr.RISK_ENGINE.Rules.Frequency;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;
import com.sr.RISK_ENGINE.util.EventUtils;

public class PortScanFrequencyRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {

        Deque<AssetEvent> events = context.getRecentEvents();

        EventUtils eventUtils = new EventUtils(context);

        if (events.isEmpty())
            return 0.0;

        int portScans = eventUtils.findRecent(events, EventType.PORT_SCAN, 60, events.peekLast().timestamp());

        if (portScans >= 40)
            return 60.0;
        else if (portScans >= 20)
            return 40.0;
        else if (portScans >= 10)
            return 25.0;
        else if (portScans >= 5)
            return 10.0;
        return 0.0;
    }
}
