package com.sr.RISK_ENGINE.util;

import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;

public class EventUtils {
    private final Deque<AssetEvent> allEvents;
    private final List<AssetEvent> freshEvents;
    
    // Define your "Freshness" window here (e.g., 5 minutes)
    private static final int FRESHNESS_THRESHOLD_SECONDS = 300; 

    public EventUtils(AssetContext context) {
        this.allEvents = context.getRecentEvents();
        // Global Filter: Only look at events from the last 5 minutes
        Instant cutoff = Instant.now().minusSeconds(FRESHNESS_THRESHOLD_SECONDS);
        
        this.freshEvents = allEvents.stream()
                .filter(e -> e.timestamp().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    /**
     * Rules should call this to get events that are actually "active"
     */
    public List<AssetEvent> getFreshEvents() {
        return freshEvents;
    }

    /**
     * Updated count to use the fresh list
     */
    public int countFresh(EventType eventType) {
        int count = 0;
        for (AssetEvent event : freshEvents) {
            if (event.eventType() == eventType) {
                count++;
            }
        }
        return count;
    }

    /**
     * Precise recent window check within the already fresh list
     */
    public int findRecent(EventType eventType, int withinSeconds) {
        int count = 0;
        Instant startWindow = Instant.now().minusSeconds(withinSeconds);
        for (AssetEvent event : freshEvents) {
            if (event.eventType() == eventType && event.timestamp().isAfter(startWindow)) {
                count++;
            }
        }
        return count;
    }
}