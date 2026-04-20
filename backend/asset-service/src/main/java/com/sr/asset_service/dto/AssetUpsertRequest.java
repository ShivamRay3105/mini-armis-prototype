package com.sr.asset_service.dto;

/**
 * CIS-1: Sent by RISK-ENGINE (AssetDiscoveryServiceImpl) to auto-create or enrich
 * an asset. deviceType carries the classification result (PLC, IOT, RTU, etc.)
 */
public record AssetUpsertRequest(
                String macAddress,
                String ipAddress,
                Integer port,
                String protocol,
                String deviceType   // CIS-1: classification from RISK-ENGINE discovery
) {
}

