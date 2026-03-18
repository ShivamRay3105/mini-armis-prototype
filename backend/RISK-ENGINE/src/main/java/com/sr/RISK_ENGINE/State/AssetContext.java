package com.sr.RISK_ENGINE.State;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import com.sr.RISK_ENGINE.model.AssetEvent;

public class AssetContext {

    private final Long assetId;

    private double riskScore;

    private final Deque<AssetEvent> recentEvents = new ArrayDeque<>();

    private Instant lastUpdated;

    public AssetContext(Long assetId) {
        this.assetId = assetId;
        this.lastUpdated = Instant.now();
    }

    public void addEvent(AssetEvent event) {
        recentEvents.addLast(event);
    }

    public void removeOldEvents(Instant cutoff) {
        while (!recentEvents.isEmpty() &&
                recentEvents.peekFirst().timestamp().isBefore(cutoff)) {
            recentEvents.removeFirst();
        }
    }

    public void updateRisk(double newScore) {
        this.riskScore = newScore;
        this.lastUpdated = Instant.now();
    }

    public Deque<AssetEvent> getRecentEvents() {
        return recentEvents;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public Long getAssetId() {
        return assetId;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}