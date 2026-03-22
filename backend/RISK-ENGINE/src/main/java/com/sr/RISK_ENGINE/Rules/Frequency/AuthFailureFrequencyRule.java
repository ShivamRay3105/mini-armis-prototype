package com.sr.RISK_ENGINE.Rules.Frequency;

import java.util.Deque;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;

public class AuthFailureFrequencyRule  implements RiskRule{
    @Override
    public double evaluate(AssetContext context) {
         Deque<AssetEvent> events = context.getRecentEvents();

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
    
}
