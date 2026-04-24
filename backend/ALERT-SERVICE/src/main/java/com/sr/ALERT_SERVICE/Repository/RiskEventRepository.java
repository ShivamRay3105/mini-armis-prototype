package com.sr.ALERT_SERVICE.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sr.ALERT_SERVICE.Enums.AlertSeverity;
import com.sr.ALERT_SERVICE.db.RiskEvent;

/**
 * CIS Control 8 — Audit Log Management:
 * findByIsResolvedFalse: active incident queue (unresolved alerts)
 * findBySeverity: priority filtering for SOC triage
 * countByGroupKey: incident correlation queries
 *
 * CIS Control 17 — Incident Response:
 * groupBy queries support the incident lifecycle view.
 */
public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {

    // Existing — last 50 alerts for history view
    List<RiskEvent> findTop50ByOrderByTimestampDesc();

    // CIS-17: active alerts only (not yet resolved) — SOC triage queue
    List<RiskEvent> findByIsResolvedFalseOrderByTimestampDesc();

    // CIS-17: filter alerts by severity level
    List<RiskEvent> findBySeverityOrderByTimestampDesc(AlertSeverity severity);

    // CIS-8: alerts for a specific asset (audit trail per device)
    List<RiskEvent> findByAssetIdOrderByTimestampDesc(UUID assetId);

    // CIS-8: unresolved alerts per severity (for dashboard KPIs)
    long countByIsResolvedFalseAndSeverity(AlertSeverity severity);

    // CIS-17: grouped view — all alerts sharing the same groupKey (same incident)
    @Query("SELECT r FROM RiskEvent r WHERE r.groupKey = :groupKey ORDER BY r.timestamp DESC")
    List<RiskEvent> findByGroupKey(String groupKey);
}
