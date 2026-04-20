package com.sr.RISK_ENGINE.Enums;

public enum EventType {
    // Auth Layer
    AUTH_SUCCESS,
    AUTH_FAILURE,

    // Access Control
    UNAUTHORIZED_ACCESS,
    PRIVILEGE_ESCALATION,

    // Network Behavior
    PORT_SCAN,
    SUSPICIOUS_TRAFFIC,

    // System Behavior
    ANOMALOUS_BEHAVIOR,

    // CIS-13: Behavioral anomaly detections (added for CIS compliance)
    NEW_PORT_DETECTED,      // asset communicated on a port not seen before
    UNUSUAL_PROTOCOL        // OT protocol (MODBUS/DNP3) with suspicious traffic
}
