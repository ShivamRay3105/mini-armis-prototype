package com.sr.asset_service.model;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "assets")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private Long id;

    @Column(name = "device_type", nullable = false)
    private String deviceType; // PLC, HMI, RTU

    @Column(nullable = false)
    private String protocol; // MODBUS, DNP3

    @Column(nullable = false)
    private String zone; // OT_ZONE_1

    @Column(name = "ip_address", nullable = false, unique = true)
    private String ipAddress;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; // ACTIVE, MAINTENANCE, DECOMMISSIONED

    @Column(name = "last_seen")
    private Instant lastSeen;
}