package com.sr.RISK_ENGINE.Rules.Sequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;

public class PortScanToAuthFailureRule implements RiskRule {
    // PORT_SCAN → AUTH_FAILURE 20
    // heavy scan → auth 30
    @Override
    public double evaluate(AssetContext context) {

        Deque<AssetEvent> events = context.getRecentEvents();

        List<AssetEvent> eventList = new ArrayList<>(events);

        double finalScore = 0.0;

        for (int i = 0; i < events.size(); i++) {

            if (eventList.get(i).eventType() == EventType.AUTH_FAILURE) {

                double score = 0.0;

                for (int j = i - 1; j >= 0; j--) {

                    long timeDifference = Duration.between(eventList.get(j).timestamp(), eventList.get(i).timestamp())
                            .toSeconds();
                    if (timeDifference > 60)
                        break;

                    if (eventList.get(j).eventType() == EventType.PORT_SCAN) {
                        if (timeDifference <= 30)
                            score = Math.max(score, 30.0);
                        else
                            score = Math.max(score, 20.0);
                        break; // max score for this sequence of auth events
                    }
                }
                finalScore = Math.max(finalScore, score);
            }
        }
        return finalScore;
    }
}
