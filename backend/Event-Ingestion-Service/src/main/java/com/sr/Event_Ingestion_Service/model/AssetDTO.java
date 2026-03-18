package com.sr.Event_Ingestion_Service.model;

public record AssetDTO(
                Long assetId,
                String protocol,
                String ipAddress,
                String deviceType,
                Status status) {
}