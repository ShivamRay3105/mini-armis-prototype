package com.sr.ALERT_SERVICE.Controller;

import org.springframework.web.bind.annotation.*;

import com.sr.ALERT_SERVICE.Enums.AlertSeverity;
import com.sr.ALERT_SERVICE.Repository.RiskEventRepository;
import com.sr.ALERT_SERVICE.Service.AlertConsumerService;
import com.sr.ALERT_SERVICE.db.RiskEvent;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CIS Control 17 — Incident Response:
 *   /active   → unresolved alert queue for SOC
 *   /grouped  → incident correlation view
 *   /history?severity=HIGH → priority-filtered history
 *
 * CIS Control 8 — Audit Log Management:
 *   /asset/{assetId} → per-device audit trail
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final RiskEventRepository repository;
    private final AlertConsumerService alertConsumerService;

    // ── Existing ─────────────────────────────────────────────────────────────

    /** Returns last 50 alerts. Optionally filter by severity (e.g. ?severity=HIGH) */
    @GetMapping("/history")
    public List<RiskEvent> getAlertHistory(
            @RequestParam(required = false) String severity) {

        if (severity != null && !severity.isBlank()) {
            try {
                AlertSeverity level = AlertSeverity.valueOf(severity.toUpperCase());
                return repository.findBySeverityOrderByTimestampDesc(level);
            } catch (IllegalArgumentException e) {
                // fall through to default if invalid value passed
            }
        }
        return repository.findTop50ByOrderByTimestampDesc();
    }

    @PostMapping("/{id}/resolve")
    public void alertResolve(@PathVariable Long id) {
        alertConsumerService.resolveAlert(id);
    }

    // ── CIS-17: Incident Response ─────────────────────────────────────────────

    /**
     * Active alert queue — only unresolved alerts, newest first.
     * This is the primary SOC triage view (CIS-17).
     */
    @GetMapping("/active")
    public List<RiskEvent> getActiveAlerts() {
        return repository.findByIsResolvedFalseOrderByTimestampDesc();
    }

    /**
     * Grouped incident view — alerts grouped by groupKey (assetId:riskLevel).
     * Each group represents one incident. CIS-17 incident lifecycle.
     */
    @GetMapping("/grouped")
    public Map<String, List<RiskEvent>> getGroupedAlerts() {
        List<RiskEvent> activeAlerts = repository.findByIsResolvedFalseOrderByTimestampDesc();
        return activeAlerts.stream()
                .filter(e -> e.getGroupKey() != null)
                .collect(Collectors.groupingBy(RiskEvent::getGroupKey));
    }

    /**
     * Per-device audit trail — all alerts for a specific asset UUID.
     * CIS-8: supports audit query / log replay per device.
     */
    @GetMapping("/asset/{assetId}")
    public List<RiskEvent> getAlertsByAsset(@PathVariable UUID assetId) {
        return repository.findByAssetIdOrderByTimestampDesc(assetId);
    }

    /**
     * Dashboard KPIs — count of unresolved alerts per severity level.
     * CIS-8: supports management reporting and trend analysis.
     */
    @GetMapping("/summary")
    public Map<String, Long> getAlertSummary() {
        return Map.of(
                "CRITICAL", repository.countByIsResolvedFalseAndSeverity(AlertSeverity.CRITICAL),
                "HIGH",     repository.countByIsResolvedFalseAndSeverity(AlertSeverity.HIGH),
                "MEDIUM",   repository.countByIsResolvedFalseAndSeverity(AlertSeverity.MEDIUM),
                "LOW",      repository.countByIsResolvedFalseAndSeverity(AlertSeverity.LOW)
        );
    }
}