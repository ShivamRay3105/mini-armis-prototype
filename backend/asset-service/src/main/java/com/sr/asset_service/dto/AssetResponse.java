package com.sr.asset_service.dto;

import java.time.Instant;

import com.sr.asset_service.model.Status;

public record AssetResponse(
                Long id,
                String deviceType,
                String protocol,
                String zone,
                String ipAddress,
                Integer riskScore,
                Status status,
                Instant lastSeen) {
}
