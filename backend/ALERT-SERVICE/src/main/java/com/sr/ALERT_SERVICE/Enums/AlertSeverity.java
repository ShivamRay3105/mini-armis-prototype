package com.sr.ALERT_SERVICE.Enums;

/**
 * CIS Control 17 — Incident Response:
 * Standardised severity levels mapped from risk score thresholds.
 * These match the RISK-ENGINE's RiskLevel enum so the two services speak the same language.
 *
 * Thresholds (mirrors RISK-ENGINE logic):
 *   CRITICAL  ≥ 80
 *   HIGH      ≥ 50
 *   MEDIUM    ≥ 20
 *   LOW       <  20
 */
public enum AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static AlertSeverity fromScore(double score) {
        if (score >= 80) return CRITICAL;
        if (score >= 50) return HIGH;
        if (score >= 20) return MEDIUM;
        return LOW;
    }
}
