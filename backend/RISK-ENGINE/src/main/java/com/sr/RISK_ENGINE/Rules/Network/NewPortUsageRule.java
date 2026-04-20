package com.sr.RISK_ENGINE.Rules.Network;

import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;

import java.util.Deque;
import java.util.ArrayList;
import java.util.List;

/**
 * CIS Control 13 — Network Monitoring & Defense
 *
 * Detects when an asset communicates on a port it has NEVER used before.
 * This is a classic reconnaissance / lateral movement signal in OT networks.
 *
 * Scoring:
 *   - 1 new port in last window  → +15 risk
 *   - 2+ new ports in last window → +25 risk (more aggressive exploration)
 */
public class NewPortUsageRule implements RiskRule {

    @Override
    public double evaluate(AssetContext context) {
        Deque<AssetEvent> events = context.getRecentEvents();
        if (events.isEmpty()) return 0.0;

        // Look at recent events only (last 50 — lightweight, not a full scan)
        List<AssetEvent> recentList = new ArrayList<>(events);
        int size = recentList.size();
        List<AssetEvent> window = recentList.subList(Math.max(0, size - 50), size);

        int newPortCount = 0;
        for (AssetEvent event : window) {
            if (event.port() != null) {
                boolean isNew = context.addPortAndCheckIfNew(event.port());
                if (isNew) {
                    newPortCount++;
                }
            }
        }

        if (newPortCount >= 2) return 25.0;
        if (newPortCount == 1) return 15.0;

        return 0.0;
    }
}
