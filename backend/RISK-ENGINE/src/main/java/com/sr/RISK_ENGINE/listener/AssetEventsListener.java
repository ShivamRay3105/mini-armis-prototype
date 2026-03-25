package com.sr.RISK_ENGINE.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.model.AssetEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetEventsListener {

    private final RiskService riskService;

    @KafkaListener(topics = "asset-events", groupId = "risk-engine-group")
    public void consume(AssetEvent event) {
        System.out.println("Received event: " + event);
        riskService.processEvent(event);
    }
}
