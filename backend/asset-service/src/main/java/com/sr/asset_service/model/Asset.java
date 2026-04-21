package com.sr.asset_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sr.asset_service.Enums.DeviceType;
import com.sr.asset_service.Enums.RiskLevel;

@Entity
@Table(name = "assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assetId; // ← was mixing UUID & Long, now UUID everywhere

    @Column(name = "mac_address", unique = true, length = 17)
    private String macAddress; // ← NEW: primary identity field

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "zone", length = 50)
    private String zone; // IT / OT / IoT

    @Column(name = "device_type", length = 50)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "vendor")
    private String vendor;

    // Lifecycle
    @Column(name = "first_seen", nullable = false, updatable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status; // ← keep Status enum, drop isActive boolean

    // Behavioral profile (NEW fields)
    @Column(name = "communication_count")
    private long communicationCount;

    @ElementCollection
    @Builder.Default
    private Set<Integer> portsUsed = new HashSet<>(); // NEW

    @Column(name = "protocols_seen", columnDefinition = "varchar(50)[]")
    @Builder.Default
    private Set<String> protocolsSeen = new HashSet<>();

    // Risk
    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "risk_level", length = 10)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;// ← NEW

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "protocol", length = 50)
    private String protocol; // ← your existing DTO field

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        firstSeen = Instant.now();
        lastSeen = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}