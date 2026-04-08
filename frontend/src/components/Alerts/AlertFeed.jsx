import React from 'react';
import { useWebSockets } from '../../hooks/useWebSockets';

const AlertFeed = () => {
    // Topic matches your backend MessagingTemplate destination
    const alerts = useWebSockets('/topic/alerts');

    return (
        <div className="alert-feed-container">
            <h3 className="alert-feed-title">Live Critical Threats</h3>
            <div className="alert-list">
                {alerts.length === 0 && (
                    <p className="no-telemetry">Waiting for incoming telemetry...</p>
                )}
                
                {/* Note: Matching 'riskLevel' from your RiskEvent.java backend entity */}
               {alerts.map((alert, index) => (
    <div key={index} className="alert-card critical">
        <div className="alert-header">
            <span className="timestamp">{new Date(alert.timestamp).toLocaleTimeString()}</span>
            <span className="status-badge">CRITICAL_THREAT</span>
        </div>
        <div className="alert-body">
            {/* assetId 13 becomes meaningful here */}
            <p><strong>Target:</strong> Asset ID {alert.assetId}</p>
            <p><strong>Incident:</strong> {alert.reason}</p>
        </div>
        <div className="alert-footer">
            <span>Risk Impact: {alert.riskScore}%</span>
        </div>
    </div>
))}
            </div>
        </div>
    );
};

export default AlertFeed;