package com.sr.RISK_ENGINE.State;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sr.RISK_ENGINE.model.AssetEvent;

public class AssetContext {

    private final UUID assetId;
    private double riskScore;
    private final Deque<AssetEvent> recentEvents;
    private Instant lastUpdated;
    private boolean requiresManualReset = false;

    // CIS-13: tracks all ports this asset has ever communicated on (persisted in Redis)
    private Set<Integer> knownPorts = new HashSet<>();

    // Tell Jackson to use THIS constructor when deserializing from Redis
    @JsonCreator
    public AssetContext(@JsonProperty("assetId") UUID assetId) {
        this.assetId = assetId;
        this.recentEvents = new ArrayDeque<>();
        this.lastUpdated = Instant.now();
        this.knownPorts = new HashSet<>();
    }

    public void addEvent(AssetEvent event) {
        recentEvents.addLast(event);
    }

    public void removeOldEvents() {
        while (recentEvents.size() > 1000)
            recentEvents.removeFirst();
    }

    /**
     * CIS-13: Returns true if this port is NEW (not seen before) for this asset.
     * Side-effect: registers the port into knownPorts.
     * Used by NewPortUsageRule.
     */
    public boolean addPortAndCheckIfNew(Integer port) {
        if (port == null) return false;
        return knownPorts.add(port); // Set.add() returns true only when element is newly added
    }

    public void updateRisk(double newScore, Instant eventTime) {
        this.riskScore = newScore;
        this.lastUpdated = eventTime;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public boolean isRequiresManualReset() {
        return requiresManualReset;
    }

    public void setRequiresManualReset(boolean requiresManualReset) {
        this.requiresManualReset = requiresManualReset;
    }

    public void resetLock() {
        this.requiresManualReset = false;
        this.lastUpdated = Instant.now();
    }

    public Set<Integer> getKnownPorts() {
        return knownPorts;
    }

    public Deque<AssetEvent> getRecentEvents() {
        return recentEvents;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}