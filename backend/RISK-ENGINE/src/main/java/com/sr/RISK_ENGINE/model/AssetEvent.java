package com.sr.RISK_ENGINE.model;

import java.time.Instant;
import java.util.UUID;

public record AssetEvent(

                UUID eventId,
                String version,
                Long assetId,
                EventType eventType,
                Instant timestamp,
                Instant ingestedAt,
                String protocol,
                String ipAddress) {

}
