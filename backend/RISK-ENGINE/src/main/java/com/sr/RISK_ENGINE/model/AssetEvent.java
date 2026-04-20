package com.sr.RISK_ENGINE.model;

import java.time.Instant;
import java.util.UUID;

import com.sr.RISK_ENGINE.Enums.EventType;

public record AssetEvent(

                UUID eventId,
                String version,
                UUID assetId,
                EventType eventType,
                Instant timestamp,
                Instant ingestedAt,
                String protocol,
                String ipAddress,
                Integer port,        // CIS-13: port tracking for anomaly detection
                String macAddress    // CIS-1: stable device identity for discovery
) {

}
