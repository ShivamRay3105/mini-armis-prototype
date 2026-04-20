package com.sr.RISK_ENGINE.dto;

/**
 * CIS-1: Request sent from RISK-ENGINE to asset-service /internal/upsert
 * to auto-create or enrich an asset record discovered via Kafka events.
 */
public record AssetUpsertRequest(
        String macAddress,
        String ipAddress,
        Integer port,
        String protocol,
        String deviceType    // CIS-1: classification result (PLC, IOT, RTU, etc.)
) {}
