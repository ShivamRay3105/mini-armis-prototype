package com.sr.RISK_ENGINE.listener;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.AssetDiscoveryService;
import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.model.AssetEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CIS Control 1 + Control 8:
 * Listens to every asset-event from Kafka.
 *   1. Triggers asset auto-discovery/enrichment (CIS-1)
 *   2. Delegates risk calculation to RiskService (CIS-7, CIS-13)
 *
 * Doing both here is intentional — RISK-ENGINE already owns this topic,
 * so piggy-backing discovery costs zero extra infrastructure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetEventsListener {

    private final RiskService riskService;
    private final AssetDiscoveryService assetDiscoveryService;  // CIS-1

    @KafkaListener(topics = "asset-events", groupId = "risk-engine-group")
    public void consume(AssetEvent event) {
        MDC.put("eventId", event.eventId().toString());
        try {
            log.info("Received event: {} for asset: {}", event.eventId(), event.assetId());

            // ── Step 1: CIS-1 — Auto-discover / enrich asset profile ───────
            // This call is fire-and-forget: failures are caught inside the impl
            // and must NEVER prevent risk processing.
            assetDiscoveryService.discoverAndEnrich(event);

            // ── Step 2: CIS-7/13 — Run risk rules on the event ────────────
            riskService.processEvent(event);

        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.eventId(), e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
