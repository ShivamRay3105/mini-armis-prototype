package com.sr.RISK_ENGINE.Rules.Frequency;

import com.sr.RISK_ENGINE.Enums.EventType;
import com.sr.RISK_ENGINE.Rules.RiskRule;
import com.sr.RISK_ENGINE.State.AssetContext;

import com.sr.RISK_ENGINE.util.EventUtils;

public class PortScanFrequencyRule implements RiskRule {
    @Override
    public double evaluate(AssetContext context) {
        EventUtils eventUtils = new EventUtils(context);

        // Using the findRecent from the already-filtered fresh list
        int portScans = eventUtils.findRecent(EventType.PORT_SCAN, 60);

        if (portScans >= 40)
            return 60.0;
        else if (portScans >= 20)
            return 40.0;
        else if (portScans >= 10)
            return 25.0;
        else if (portScans >= 5)
            return 10.0;

        return 0.0;
    }
}