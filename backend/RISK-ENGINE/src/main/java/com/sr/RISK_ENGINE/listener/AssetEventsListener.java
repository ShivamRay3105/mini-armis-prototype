package com.sr.RISK_ENGINE.listener;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.model.AssetEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetEventsListener {

    private final RiskService riskService;

    @KafkaListener(topics = "asset-events", groupId = "risk-engine-group")
    public void consume(AssetEvent event) {
        MDC.put("eventId", event.eventId().toString());
        try {
            // System.out.println("Received event: " + event);
            log.info("Received event: {} for asset: {}", event.eventId(), event.assetId());
            riskService.processEvent(event);
        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.eventId(), e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
