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
                "Unauthorized access attempt detected on asset %s. Indicates possible credential misuse or intrusion attempt.",

                EventType.PRIVILEGE_ESCALATION,
                "Privilege escalation detected on asset %s. Potential compromise of access controls.",

                EventType.AUTH_FAILURE,
                "Multiple authentication failures on asset %s. Possible brute-force attack.",

                EventType.PORT_SCAN,
                "Port scanning activity detected on asset %s. Indicates reconnaissance behavior.");

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