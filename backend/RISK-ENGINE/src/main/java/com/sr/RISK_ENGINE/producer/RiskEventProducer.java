package com.sr.RISK_ENGINE.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.model.RiskEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RiskEventProducer {
    private final KafkaTemplate<String, RiskEvent> kafkaTemplate;
    private static final String TOPIC = "risk-alerts";

    public void sendRiskAlert(RiskEvent riskEvent) {
        log.info("!!! TRIGGERING SECURITY ALERT !!! Asset ID: {}, Score: {}, Level: {}",
                riskEvent.assetId(), riskEvent.riskScore(), riskLevel(riskEvent));

        // This sends the alert to the 'risk-alerts' topic
        kafkaTemplate.send(TOPIC, riskEvent.assetId().toString(), riskEvent);

        log.info("Risk Alert successfully pushed to Kafka topic: {}", TOPIC);
    }

    private String riskLevel(RiskEvent event) {
        return event.riskLevel();
    }
}