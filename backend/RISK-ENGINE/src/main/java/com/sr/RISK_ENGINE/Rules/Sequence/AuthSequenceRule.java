package com.sr.RISK_ENGINE.Rules.Sequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;

public class AuthSequenceRule implements RiskRule {
 @Override
    public double evaluate(AssetContext context) {
        
        Deque<AssetEvent> events = context.getRecentEvents();

        List<AssetEvent> eventList = new ArrayList<>(events);

        double score = 0.0;

        for (int i = 0; i < eventList.size(); i++) {

            if (eventList.get(i).eventType() == EventType.UNAUTHORIZED_ACCESS) {

                // look backward
                for (int j = i - 1; j >= 0; j--) {

                    if (score == 50.0)
                        break; // max score for this sequence of auth events

                    long timeDifference = Duration.between(eventList.get(j).timestamp(), eventList.get(i).timestamp())
                            .toSeconds();
                    if (timeDifference > 120)
                        break;

                    if (eventList.get(j).eventType() == EventType.AUTH_FAILURE) {
                        score = Math.max(score, 40.0);
                    } else if (eventList.get(j).eventType() == EventType.AUTH_SUCCESS) {
                        score = Math.max(score, 50.0);
                    }
                }
            }
        }
        return score;
    }

}
