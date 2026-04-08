import React, { useEffect, useState } from 'react';
import useAxiosPrivate from '../../hooks/useAxiosPrivate';
import AlertFeed from '../Alerts/AlertFeed';

const AnalystDashboard = () => {
    const [assets, setAssets] = useState([]);
    const [history, setHistory] = useState([]);
    const axiosPrivate = useAxiosPrivate();

    useEffect(() => {
        const loadInitialData = async () => {
            try {
                // Notice the paths match your Gateway predicates exactly
                const [assetRes, historyRes] = await Promise.all([
                    axiosPrivate.get('/api/v1/assets'),
                    axiosPrivate.get('/api/alerts/history')
                ]);
                setAssets(assetRes.data);
                setHistory(historyRes.data);
            } catch (err) {
                console.error("SentinelStream Data Fetch Failure", err);
            }
        };
        loadInitialData();
    }, [axiosPrivate]);

    return (
        <div className="dashboard-wrapper">
            <header className="dashboard-header">
                <div className="header-status">
                    <span className="pulse-icon"></span>
                    <h1>SYSTEM_ANALYST_CONSOLE // LIVE_TELEMETRY</h1>
                </div>
                <div className="system-time">{new Date().toLocaleTimeString()}</div>
            </header>

            <main className="dashboard-main">
                {/* Asset Inventory Grid */}
                <section className="asset-grid">
                    <div className="section-header">
                        <h2>Connected Industrial Assets</h2>
                        <span className="count">{assets.length} Active Devices</span>
                    </div>
                    <div className="grid-container">
                        {assets.map((asset, index) => (
                            <div key={asset.assetId || `asset-fallback-${index}`} className="asset-card">
                                <div className="card-top">
                                    <p className="mac-address">{asset.macAddress || 'ID: ' + asset.assetId}</p>
                                    <span className={`status-tag ${asset.riskScore > 70 ? 'danger' : 'safe'}`}>
                                        {asset.riskScore > 70 ? 'THREAT' : 'STABLE'}
                                    </span>
                                </div>
                                
                                <h3>{asset.name || `PLC_NODE_${asset.assetId}`}</h3>
                                
                                <div className="risk-display">
                                    <div className="risk-label">Vulnerability Index:</div>
                                    <div className={`risk-indicator ${asset.riskScore > 70 ? 'high' : 'low'}`}>
                                        {asset.riskScore}% 
                                    </div>
                                </div>

                                <div className="progress-bg">
                                    <div 
                                        className="progress-fill" 
                                        style={{ width: `${asset.riskScore}%`, backgroundColor: asset.riskScore > 70 ? '#ff4d4d' : '#00ff88' }}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>

                {/* Live Sidebar */}
                <aside className="dashboard-sidebar">
                    <div className="sidebar-header">
                        <h2>INCIDENT_RESPONSE_FEED</h2>
                    </div>
                    <AlertFeed />
                </aside>
            </main>
        </div>
    );
};

export default AnalystDashboard;