import React from "react";
import { useWebSockets } from "../../hooks/useWebSockets";
import useAxiosPrivate from "../../hooks/useAxiosPrivate";

const AlertFeed = () => {
  // 1. Listen for live updates (including resolution broadcasts)
  const alerts = useWebSockets("/topic/alerts");
  const axiosPrivate = useAxiosPrivate();

  // 2. Handle the "Mark as Resolved" action
  const handleResolve = async (alertId) => {
    try {
      // Logic: Matches @PostMapping("/{id}/resolve")
      // We inject the alertId directly into the URL string
      await axiosPrivate.post(`/api/alerts/${alertId}/resolve`);
      
      console.log(`Alert ${alertId} resolution request sent.`);
    } catch (err) {
      console.error("Failed to resolve alert:", err);
    }
  };

  return (
    <section className="alert-feed-container">
      <header>
        <h3>Live Security Threats</h3>
      </header>

      <div className="alert-list">
        {/* Logic: Only show alerts that are NOT resolved */}
        {alerts.filter((a) => !a.isResolved).length === 0 ? (
          <p className="status-stable">System Stable: No active threats.</p>
        ) : (
          alerts
            .filter((a) => !a.isResolved)
            .map((alert) => (
              <article key={alert.alertId} className="alert-card">
                <header className="alert-meta">
                  <time>
                    {new Date(alert.timestamp).toLocaleTimeString()}
                  </time>
                  <span className="severity-label">{alert.riskLevel}</span>
                </header>

                <div className="alert-details">
                  <p>
                    <strong>Asset:</strong> {alert.assetId}
                  </p>
                  <p>
                    <strong>Reason:</strong> {alert.reason}
                  </p>
                </div>

                <footer className="alert-actions-area">
                  <div className="impact-data">
                    Risk Score: {alert.riskScore}%
                  </div>
                  
                  {/* The specific button for this alert card */}
                  <div className="button-group">
                    <button 
                      type="button"
                      onClick={() => handleResolve(alert.alertId)}
                    >
                      Resolve Incident
                    </button>
                  </div>
                </footer>
              </article>
            ))
        )}
      </div>
    </section>
  );
};

export default AlertFeed;