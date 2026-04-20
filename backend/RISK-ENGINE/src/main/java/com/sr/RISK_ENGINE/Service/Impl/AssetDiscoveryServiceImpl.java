package com.sr.RISK_ENGINE.Service.Impl;

import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Client.AssetServiceClient;
import com.sr.RISK_ENGINE.Service.AssetDiscoveryService;
import com.sr.RISK_ENGINE.dto.AssetUpsertRequest;
import com.sr.RISK_ENGINE.model.AssetEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CIS Control 1 — Inventory & Control of Assets
 *
 * The RISK-ENGINE already consumes every event from the asset-events topic.
 * We piggyback on that consumption to auto-discover / enrich assets in the
 * asset-service — no new service, no extra Kafka topic, zero overhead.
 *
 * Classification rules:
 *   port 502  → MODBUS → PLC (Programmable Logic Controller)
 *   port 1883 → MQTT   → IoT device
 *   port 20000/DNP3 → RTU/SCADA
 *   default   → UNKNOWN (human can classify later)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssetDiscoveryServiceImpl implements AssetDiscoveryService {

    private final AssetServiceClient assetServiceClient;

    @Override
    public void discoverAndEnrich(AssetEvent event) {
        try {
            String deviceType = classifyDevice(event.port(), event.protocol());

            AssetUpsertRequest request = new AssetUpsertRequest(
                    event.macAddress(),
                    event.ipAddress(),
                    event.port(),
                    event.protocol(),
                    deviceType
            );

            log.info("[CIS-1] Auto-discovery: asset MAC={} IP={} port={} classified as {}",
                    event.macAddress(), event.ipAddress(), event.port(), deviceType);

            assetServiceClient.upsertAsset(request);

        } catch (Exception e) {
            // Discovery failures must NOT disrupt the risk pipeline
            log.warn("[CIS-1] Asset discovery upsert failed for event {}: {}", event.eventId(), e.getMessage());
        }
    }

    /**
     * Simple port/protocol-based device classification.
     * CIS-1: Every discovered asset gets a device type so the inventory
     * is not just "a list of IPs" but a typed, meaningful asset register.
     */
    private String classifyDevice(Integer port, String protocol) {
        if (port != null) {
            return switch (port) {
                case 502  -> "PLC";          // MODBUS — Programmable Logic Controller
                case 1883 -> "IOT";          // MQTT — IoT sensor/actuator
                case 20000 -> "RTU";         // DNP3 — Remote Terminal Unit (SCADA)
                case 102  -> "PLC";          // S7COMM — Siemens PLC
                case 44818 -> "PLC";         // EtherNet/IP — Allen-Bradley PLC
                case 47808 -> "BMS";         // BACnet — Building Management System
                default   -> "UNKNOWN";
            };
        }
        if (protocol != null) {
            return switch (protocol.toUpperCase()) {
                case "MODBUS" -> "PLC";
                case "DNP3"   -> "RTU";
                case "MQTT"   -> "IOT";
                case "S7COMM" -> "PLC";
                default       -> "UNKNOWN";
            };
        }
        return "UNKNOWN";
    }
}
