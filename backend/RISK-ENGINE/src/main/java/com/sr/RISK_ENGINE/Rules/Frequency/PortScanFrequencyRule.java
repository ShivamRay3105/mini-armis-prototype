package com.sr.RISK_ENGINE.Rules.Frequency;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;

public class PortScanFrequencyRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {
        Deque<AssetEvent> events = context.getRecentEvents();

        int portScans = 0;
        if(events.isEmpty()) return 0.0;
        Instant currentTime = events.peekLast().timestamp(); // most recent event time

        for (AssetEvent event : events) {
            if (event.eventType() == EventType.PORT_SCAN) {
                long timeDifference = Duration.between(event.timestamp(), currentTime).toSeconds();
                if (timeDifference <= 60) {
                    portScans++;
                }
            }
        }

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
