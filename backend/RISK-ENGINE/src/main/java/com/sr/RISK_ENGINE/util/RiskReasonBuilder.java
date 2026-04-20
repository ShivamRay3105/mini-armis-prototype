package com.sr.RISK_ENGINE.util;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;

@Service
public class RiskReasonBuilder {

    public String build(AssetContext assetContext,
            AssetEvent event,
            double riskScore,
            String severity) {

        String assetId = assetContext.getAssetId().toString();

        Map<EventType, String> templates = Map.of(
                EventType.UNAUTHORIZED_ACCESS,
                "[MITRE: TA0001 - Initial Access] Unauthorized access attempt detected on asset %s. Indicates possible credential misuse or intrusion attempt.",

                EventType.PRIVILEGE_ESCALATION,
                "[MITRE: TA0004 - Privilege Escalation] Privilege escalation detected on asset %s. Potential compromise of access controls.",

                EventType.AUTH_FAILURE,
                "[MITRE: TA0006 - Credential Access] Multiple authentication failures on asset %s. Possible brute-force attack.",

                EventType.PORT_SCAN,
                "[MITRE: TA0043 - Reconnaissance] Port scanning activity detected on asset %s. Indicates reconnaissance behavior.",

                EventType.NEW_PORT_DETECTED,
                "[MITRE: TA0007 - Discovery] Asset %s communicated on a previously-unseen port. Possible lateral movement or new service exposure.",

                EventType.UNUSUAL_PROTOCOL,
                "[MITRE: TA0040 - Impact] Suspicious traffic detected on OT protocol for asset %s. Possible protocol abuse or Man-in-the-Middle attempt.",

                EventType.SUSPICIOUS_TRAFFIC,
                "[MITRE: TA0011 - Command and Control] Suspicious network traffic detected on asset %s. Possible C2 communication or data exfiltration.",

                EventType.ANOMALOUS_BEHAVIOR,
                "[MITRE: TA0002 - Execution] Anomalous behavior detected on asset %s. Deviates significantly from established baseline.");

        String baseMessage;

        if (event != null && templates.containsKey(event.eventType())) {
            baseMessage = String.format(templates.get(event.eventType()), assetId);
        } else if (event != null) {
            baseMessage = "Suspicious activity (" + event.eventType() + ") detected on asset " + assetId;
        } else {
            baseMessage = "Risk score updated for asset " + assetId;
        }

        return String.format(
                "[%s] %s (Risk Score: %.2f)",
                severity,
                baseMessage,
                riskScore);
    }
}

/*
 * future enhancements for reason
 * 
 * 1. External config (no redeploys)
 * 
 * Move templates to YAML/DB
 * 
 * 2. Localization (if needed)
 * 
 * Different languages
 * 
 * 3. Security enrichment
 * 
 * MITRE ATT&CK mapping
 * CVE references
 * 
 * 4. Event metadata usage
 * 
 * "Port scan from IP " + event.getSourceIp()
 * 
 * Example:
 */