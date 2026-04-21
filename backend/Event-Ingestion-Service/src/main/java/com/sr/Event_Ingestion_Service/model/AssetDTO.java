package com.sr.Event_Ingestion_Service.model;

import java.util.UUID;

public record AssetDTO(
        UUID assetId,
        String protocol,
        String ipAddress,
        String deviceType,
        String macAddress,
        Status status) {
}