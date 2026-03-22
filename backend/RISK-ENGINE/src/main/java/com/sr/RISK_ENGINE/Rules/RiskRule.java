package com.sr.RISK_ENGINE.Rules;

import com.sr.RISK_ENGINE.State.AssetContext;

public interface RiskRule {
    double evaluate(AssetContext context);
}
