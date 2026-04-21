package com.sr.ALERT_SERVICE.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import com.sr.ALERT_SERVICE.Enums.AlertSeverity;

/**
 * CIS Control 8 — Audit Log Management:
 * Added: severity (enum), groupKey (incident grouping key)
 * These turn the alert table from a raw log into a queryable incident store.
 *
 * CIS Control 17 — Incident Response:
 * severity enables priority-based response queues.
 * groupKey enables incident correlation (same asset + same level = one
 * incident).
 */
@Data
@Entity
@Table(name = "risk_events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    UUID eventId;

    // NOTE: assetId is stored as UUID string for forward-compatibility.
    // The RISK-ENGINE uses UUID for assetId; older data may have Long — migrate
    // later.
    @Column(name = "asset_id")
    UUID assetId;

    Double riskScore;
    String riskLevel;
    String reason;
    Instant timestamp;

    // CIS-17: severity for priority-based incident response queues
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private AlertSeverity severity;

    // CIS-8: groupKey for incident correlation
    // Format: "<assetId>:<riskLevel>" e.g. "550e8400-...:HIGH"
    // Alerts with the same groupKey belong to the same incident.
    @Column(name = "group_key")
    private String groupKey;

    @Column(name = "is_resolved", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isResolved = false;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}
