package com.sr.RISK_ENGINE.Service;

import com.sr.RISK_ENGINE.model.AssetEvent;

/**
 * CIS Control 1 — Inventory & Control of Assets
 *
 * Responsible for translating a raw network event into an asset upsert call
 * to the asset-service. This runs inside the RISK-ENGINE, which already
 * consumes the asset-events Kafka topic — zero extra infrastructure needed.
 */
public interface AssetDiscoveryService {

    /**
     * Given a network event, classify the device type and call asset-service
     * /internal/upsert to auto-create or enrich the asset record.
     */
    void discoverAndEnrich(AssetEvent event);
}
