package com.sr.RISK_ENGINE.Service;

import com.sr.RISK_ENGINE.model.AssetEvent;

public interface RiskService {

    void processEvent(AssetEvent event);

}
