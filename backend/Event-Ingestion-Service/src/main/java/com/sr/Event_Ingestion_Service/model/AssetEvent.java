package com.sr.Event_Ingestion_Service.model;

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
        String ipAddress,
        String deviceType,
        Integer port,       // CIS-13: port for risk engine anomaly detection
        String macAddress   // CIS-1: stable identity for asset discovery enrichment

) {

}
