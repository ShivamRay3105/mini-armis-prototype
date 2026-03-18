package com.sr.RISK_ENGINE.Service.Impl;

import org.springframework.stereotype.Service;

import com.sr.RISK_ENGINE.Service.RiskService;
import com.sr.RISK_ENGINE.State.AssetContext;
import com.sr.RISK_ENGINE.model.AssetEvent;

@Service
public class RiskServiceImpl implements RiskService {

    public void processEvent(AssetEvent event) {
        // AssetContext ctx = getContext(event.assetId());

        // 1. Add base risk
        // ctx.riskScore += getBaseScore(event.eventType());

        // 2. Store event
        // ctx.recentEvents.add(event.eventType());

        // 3. Apply advanced rules
        // applyTimeRules(ctx, event);
        // applySequenceRules(ctx);
    }
}
