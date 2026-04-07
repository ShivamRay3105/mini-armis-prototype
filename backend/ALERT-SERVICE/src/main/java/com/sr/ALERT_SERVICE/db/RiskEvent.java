package com.sr.ALERT_SERVICE.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

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
    Long assetId;
    Double riskScore;
    String riskLevel;
    String reason;
    Instant timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}
