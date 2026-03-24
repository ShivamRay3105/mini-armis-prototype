package com.sr.RISK_ENGINE.State;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sr.RISK_ENGINE.model.AssetEvent;

public class AssetContext {

    private final Long assetId;
    private double riskScore;
    private final Deque<AssetEvent> recentEvents; // Stay final!
    private Instant lastUpdated;

    // Tell Jackson to use THIS constructor when reading from Redis
    @JsonCreator
    public AssetContext(@JsonProperty("assetId") Long assetId) {
        this.assetId = assetId;
        this.recentEvents = new ArrayDeque<>();
        this.lastUpdated = Instant.now();
    }

    public void addEvent(AssetEvent event) {
        recentEvents.addLast(event);
    }

    public void removeOldEvents() {
        while (recentEvents.size() > 1000)
            recentEvents.removeFirst();
    }

    public void updateRisk(double newScore, Instant eventTime) {
        this.riskScore = newScore;
        this.lastUpdated = eventTime;
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