package com.sr.ALERT_SERVICE.Service.Impl;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sr.ALERT_SERVICE.Repository.RiskEventRepository;
import com.sr.ALERT_SERVICE.Service.AlertConsumerService;
import com.sr.ALERT_SERVICE.db.AlertSeverity;
import com.sr.ALERT_SERVICE.db.RiskEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CIS Control 8 — Audit Log Management:
 *   Every consumed event is persisted with severity + groupKey so the
 *   alert store is queryable by priority and incident group.
 *
 * CIS Control 17 — Incident Response:
 *   severity drives SOC triage queues.
 *   groupKey enables incident lifecycle correlation.
 */
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

        // CIS-17: derive severity from risk score
        AlertSeverity severity = AlertSeverity.fromScore(
                event.getRiskScore() != null ? event.getRiskScore() : 0.0);
        event.setSeverity(severity);

        // CIS-8: build groupKey for incident correlation
        // Format: "<assetId>:<riskLevel>" so all CRITICAL events on the same asset share a key
        String groupKey = event.getAssetId() != null && event.getRiskLevel() != null
                ? event.getAssetId().toString() + ":" + event.getRiskLevel()
                : null;
        event.setGroupKey(groupKey);

        log.info("[CIS-8] Saving alert: severity={} groupKey={}", severity, groupKey);
        riskEventRepository.save(event);

        // Push to frontend via WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", event);
    }

    @Override
    public void resolveAlert(Long alertId) {
        RiskEvent event = riskEventRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));

        event.setResolved(true);
        riskEventRepository.save(event);
        log.info("[CIS-17] Alert {} resolved. GroupKey: {}", alertId, event.getGroupKey());

        // Notify frontend about resolution
        messagingTemplate.convertAndSend("/topic/alerts", event);
    }
}
