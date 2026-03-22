package com.sr.RISK_ENGINE.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;

import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;
import com.sr.RISK_ENGINE.model.EventType;

public class EventUtils {
    AssetContext context;
    Deque<AssetEvent> events;

    public EventUtils(AssetContext context) {
        this.context = context;
        this.events = context.getRecentEvents();
    }

    public int count(Deque<AssetEvent> events, EventType eventType) {
        int count = 0;
        for (AssetEvent event : events) {
            if (event.eventType() == eventType) {
                count++;
            }
        }
        return count;
    }

    public int findRecent(Deque<AssetEvent> events, EventType eventType, int withinSeconds) {
        int count = 0;
        Instant currentTime = Instant.now();
        for (AssetEvent event : events) {
            if (event.eventType() == eventType) {
                if (event.timestamp().isAfter(currentTime.minusSeconds(withinSeconds))) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasSequence(Deque<AssetEvent> events, EventType eventTypeA, EventType eventTypeB, int timeWindow) {
        Instant currentTime = Instant.now();
        for (AssetEvent event : events) {
            if (event.eventType() == eventTypeA) {
                if (event.timestamp().isAfter(currentTime.minusSeconds(timeWindow))) {
                    // Look for the next event of type B within the time window
                    for (AssetEvent nextEvent : events) {
                        if (nextEvent.eventType() == eventTypeB && nextEvent.timestamp().isAfter(event.timestamp())) {
                            long timeDifference = Duration.between(event.timestamp(), nextEvent.timestamp())
                                    .toSeconds();
                            if (timeDifference <= timeWindow) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
