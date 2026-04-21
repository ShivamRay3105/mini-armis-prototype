package com.sr.Event_Ingestion_Service.dto;

public record AssetUpsertRequest(
                String macAddress,
                String ipAddress,
                Integer port,
                String protocol,
                String deviceType
) {
}
