package com.sr.RISK_ENGINE.Rules.Network;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.util.EventUtils;

import java.util.Set;

/**
 * CIS Control 13 — Network Monitoring & Defense (OT-specific)
 *
 * Detects SUSPICIOUS_TRAFFIC on known OT/ICS protocols: MODBUS, DNP3, S7COMM.
 * In an OT network, these protocols should only appear in predictable,
 * structured patterns. Suspicious traffic on these protocols is a high-severity
 * indicator of possible Man-in-the-Middle, replay attacks, or protocol abuse.
 *
 * Scoring:
 *   - SUSPICIOUS_TRAFFIC + OT protocol → +20 risk (CIS-13 anomaly)
 *   - Multiple occurrences in window   → +35 risk (escalated)
 */
public class UnusualProtocolRule implements RiskRule {

    // Known OT/ICS protocols — treat suspicious traffic on these as elevated risk
    private static final Set<String> OT_PROTOCOLS = Set.of(
            "MODBUS", "DNP3", "S7COMM", "PROFINET", "BACNET", "EIP"
    );

    @Override
    public double evaluate(AssetContext context) {
        EventUtils eventUtils = new EventUtils(context);

        // Count SUSPICIOUS_TRAFFIC events on OT protocols in the 5-minute window
        long suspiciousOtEvents = eventUtils.getFreshEvents().stream()
                .filter(e -> e.eventType() == EventType.SUSPICIOUS_TRAFFIC)
                .filter(e -> e.protocol() != null && OT_PROTOCOLS.contains(e.protocol().toUpperCase()))
                .count();

        if (suspiciousOtEvents >= 3) return 35.0; // repeated OT protocol abuse
        if (suspiciousOtEvents >= 1) return 20.0; // single occurrence — still a flag

        return 0.0;
    }
}
