package com.sr.ALERT_SERVICE.Service.Impl;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sr.ALERT_SERVICE.Repository.RiskEventRepository;
import com.sr.ALERT_SERVICE.Service.AlertConsumerService;
import com.sr.ALERT_SERVICE.db.RiskEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertConsumerServiceImpl implements AlertConsumerService {

    private final RiskEventRepository riskEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String TOPIC = "risk-alerts";

    @KafkaListener(topics = TOPIC, groupId = "alert-service-group")
    public void handleRiskEvent(RiskEvent event) {
        log.info("🚨 New Threat Detected: Asset {} | Level: {}", event.getAssetId(), event.getRiskLevel());

        riskEventRepository.save(event);

        // 2. Push to Frontend via WebSocket Topic
        messagingTemplate.convertAndSend("/topic/alerts", event);
    }

    @Override
    public void resolveAlert(Long alertId) {
        RiskEvent event = riskEventRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));

        event.setResolved(true);
        riskEventRepository.save(event);

        // Notify frontend about resolution
        messagingTemplate.convertAndSend("/topic/alerts", event);
    }

}
