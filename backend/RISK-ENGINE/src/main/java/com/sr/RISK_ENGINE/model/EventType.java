package com.sr.RISK_ENGINE.model;

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
    ANOMALOUS_BEHAVIOR
}