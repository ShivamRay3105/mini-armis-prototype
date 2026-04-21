package com.sr.asset_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sr.asset_service.Enums.RiskLevel;
import com.sr.asset_service.model.Status;

public record AssetResponse(
        UUID assetId, // Long → UUID
        String macAddress, // NEW
        String deviceType,
        String protocol,
        String zone,
        String ipAddress,
        String hostname, // NEW
        String vendor, // NEW
        Integer riskScore,
        RiskLevel riskLevel, // NEW
        Status status,
        Instant firstSeen, // NEW
        Instant lastSeen,
        long communicationCount, // NEW
        List<Integer> portsUsed // NEW
) {
}