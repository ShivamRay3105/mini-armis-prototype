package com.sr.RISK_ENGINE.Rules.Sequence;

import java.time.Duration;
import java.util.List;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.util.EventUtils;
public class AuthSequenceRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {
        EventUtils eventUtils = new EventUtils(context);
        // Get only the events from the last 5 minutes
        List<AssetEvent> freshEvents = eventUtils.getFreshEvents();

        double score = 0.0;

        for (int i = 0; i < freshEvents.size(); i++) {
            // If the current event in the loop is UNAUTHORIZED_ACCESS
            if (freshEvents.get(i).eventType() == EventType.UNAUTHORIZED_ACCESS) {
                
                // Look backward within the fresh list
                for (int j = i - 1; j >= 0; j--) {
                    if (score == 50.0) break;

                    long timeDifference = Duration.between(
                        freshEvents.get(j).timestamp(), 
                        freshEvents.get(i).timestamp()
                    ).toSeconds();

                    if (timeDifference > 120) break;

                    if (freshEvents.get(j).eventType() == EventType.AUTH_FAILURE) {
                        score = Math.max(score, 40.0);
                    } else if (freshEvents.get(j).eventType() == EventType.AUTH_SUCCESS) {
                        score = Math.max(score, 50.0);
                    }
                }
            }
        }
        return score;
    }
}
