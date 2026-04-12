package com.sr.ALERT_SERVICE.Service;

import com.sr.ALERT_SERVICE.db.RiskEvent;

public interface AlertConsumerService {
    public void handleRiskEvent(RiskEvent event);

    public void resolveAlert(Long alertId);
}
