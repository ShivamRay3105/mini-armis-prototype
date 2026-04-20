package com.sr.RISK_ENGINE.model;

import java.time.Instant;
import java.util.UUID;

public record RiskEvent(
                UUID eventId,
                UUID assetId,
                Double riskScore,
                String riskLevel, // LOW, MEDIUM, HIGH, CRITICAL
                String reason, // e.g., "Multiple Auth Failures detected"
                Instant timestamp) {
}