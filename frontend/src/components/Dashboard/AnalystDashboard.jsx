import React, { useEffect, useState } from 'react';
import { 
    PieChart, Pie, Cell, 
    BarChart, Bar, 
    AreaChart, Area,
    RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar,
    XAxis, YAxis, Tooltip as RechartsTooltip, ResponsiveContainer 
} from 'recharts';
import useAxiosPrivate from '../../hooks/useAxiosPrivate';
import AlertFeed from '../Alerts/AlertFeed';
import '../../styles/AnalystDashboard.css';

const AnalystDashboard = () => {
    const [assets, setAssets] = useState([]);
    const [history, setHistory] = useState([]);
    const [selectedAsset, setSelectedAsset] = useState(null);
    const axiosPrivate = useAxiosPrivate();

    useEffect(() => {
        const loadInitialData = async () => {
            try {
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
        const interval = setInterval(loadInitialData, 5000);
        return () => clearInterval(interval);
    }, [axiosPrivate]);

    // ── DERIVED METRICS ──
    const totalAssets = assets.length;
    const criticalAssets = assets.filter(a => a.riskScore > 70).length;
    const totalAlerts = history.length;
    const avgRisk = totalAssets > 0 ? Math.round(assets.reduce((sum, a) => sum + a.riskScore, 0) / totalAssets) : 0;

    // ── CHART 1: Risk Distribution (Pie/Donut) ──
    const riskLevels = [
        { name: 'Low (0-30)', value: assets.filter(a => a.riskScore <= 30).length, color: '#10b981' },
        { name: 'Medium (31-70)', value: assets.filter(a => a.riskScore > 30 && a.riskScore <= 70).length, color: '#f59e0b' },
        { name: 'High/Critical (71+)', value: assets.filter(a => a.riskScore > 70).length, color: '#ef4444' }
    ].filter(r => r.value > 0);

    // ── CHART 2: Device Type Breakdown (BarChart) ──
    const deviceCounts = assets.reduce((acc, asset) => {
        const type = asset.deviceType || 'UNKNOWN';
        acc[type] = (acc[type] || 0) + 1;
        return acc;
    }, {});
    const deviceData = Object.keys(deviceCounts).map(k => ({ name: k, count: deviceCounts[k] }));

    // ── CHART 3: Vulnerability by Protocol (RadarChart) ──
    // Average Risk Score per Protocol
    const protocolStats = assets.reduce((acc, asset) => {
        const proto = asset.protocol || 'UNKNOWN';
        if (!acc[proto]) acc[proto] = { totalRisk: 0, count: 0 };
        acc[proto].totalRisk += asset.riskScore || 0;
        acc[proto].count += 1;
        return acc;
    }, {});
    const radarData = Object.keys(protocolStats).map(k => ({
        protocol: k,
        avgRisk: Math.round(protocolStats[k].totalRisk / protocolStats[k].count),
        fullMark: 100
    }));

    // ── CHART 4: Threat Timeline (AreaChart) ──
    // Group alerts by minute
    const timelineDataMap = history.reduce((acc, alert) => {
        // format HH:MM
        const timeKey = new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        if (!acc[timeKey]) acc[timeKey] = { time: timeKey, count: 0 };
        acc[timeKey].count += 1;
        return acc;
    }, {});
    const timelineData = Object.values(timelineDataMap).sort((a, b) => a.time.localeCompare(b.time));

    // ── CHART 5: Event Type Breakdown (BarChart) ──
    const eventTypeCounts = history.reduce((acc, alert) => {
        // Assuming alert reason looks like "Threat detected: PORT_SCAN" or similar.
        // We'll extract uppercase words with underscores to find the event type.
        const match = alert.reason?.match(/[A-Z]+_[A-Z_]+/);
        const type = match ? match[0] : 'UNKNOWN';
        acc[type] = (acc[type] || 0) + 1;
        return acc;
    }, {});
    const eventTypeData = Object.keys(eventTypeCounts).map(k => ({ name: k, count: eventTypeCounts[k] }));

    // ── FILTER STATE ──
    const [statusFilter, setStatusFilter] = useState('ALL');
    
    // Filter assets for the table
    const filteredAssets = assets.filter(asset => {
        if (statusFilter === 'ALL') return true;
        const mappedStatus = asset.riskScore > 70 ? 'THREAT' : (asset.riskScore > 30 ? 'WARNING' : 'STABLE');
        return mappedStatus === statusFilter;
    });

    return (
        <div className="dashboard-wrapper">
            <header className="dashboard-header">
                <div className="header-status">
                    <span className="pulse-icon"></span>
                    <h1>SYSTEM ANALYST CONSOLE</h1>
                </div>
            </header>

            <main className="dashboard-main">
                {/* Central Data Area */}
                <div className="central-area">
                    
                    {/* Top Metrics Row */}
                    <div className="metrics-row">
                        <div className="metric-card stable">
                            <span className="metric-title">Active Assets</span>
                            <span className="metric-value">{totalAssets}</span>
                        </div>
                        <div className={`metric-card ${criticalAssets > 0 ? 'critical' : 'stable'}`}>
                            <span className="metric-title">Critical Threats</span>
                            <span className="metric-value">{criticalAssets}</span>
                        </div>
                        <div className="metric-card warning">
                            <span className="metric-title">Total Incidents</span>
                            <span className="metric-value">{totalAlerts}</span>
                        </div>
                        <div className={`metric-card ${avgRisk > 50 ? 'warning' : 'stable'}`}>
                            <span className="metric-title">Average Risk Index</span>
                            <span className="metric-value">{avgRisk}%</span>
                        </div>
                    </div>

                    {/* Top Charts Grid (3 charts) */}
                    <div className="charts-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', gridTemplateRows: '280px' }}>
                        
                        {/* Area Chart: Threat Timeline */}
                        <div className="chart-container">
                            <div className="chart-header">Incident Timeline (Live)</div>
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={timelineData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <defs>
                                        <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#ef4444" stopOpacity={0.8}/>
                                            <stop offset="95%" stopColor="#ef4444" stopOpacity={0}/>
                                        </linearGradient>
                                    </defs>
                                    <XAxis dataKey="time" stroke="#94a3b8" fontSize={10} tickLine={false} />
                                    <YAxis stroke="#94a3b8" fontSize={10} tickLine={false} axisLine={false} />
                                    <RechartsTooltip 
                                        contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', color: '#f8fafc' }}
                                    />
                                    <Area type="monotone" dataKey="count" stroke="#ef4444" fillOpacity={1} fill="url(#colorCount)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Radar Chart: Protocol Risk */}
                        <div className="chart-container">
                            <div className="chart-header">Avg Risk by Protocol</div>
                            <ResponsiveContainer width="100%" height="100%">
                                <RadarChart cx="50%" cy="50%" outerRadius="70%" data={radarData}>
                                    <PolarGrid stroke="#334155" />
                                    <PolarAngleAxis dataKey="protocol" stroke="#94a3b8" fontSize={10} />
                                    <PolarRadiusAxis angle={30} domain={[0, 100]} stroke="#334155" tick={false} />
                                    <Radar name="Risk Score" dataKey="avgRisk" stroke="#f59e0b" fill="#f59e0b" fillOpacity={0.5} />
                                    <RechartsTooltip 
                                        contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', color: '#f8fafc' }}
                                    />
                                </RadarChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Pie Chart: Risk Distribution */}
                        <div className="chart-container">
                            <div className="chart-header">Asset Risk Distribution</div>
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={riskLevels}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={50}
                                        outerRadius={70}
                                        paddingAngle={5}
                                        dataKey="value"
                                        stroke="none"
                                    >
                                        {riskLevels.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <RechartsTooltip 
                                        contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', color: '#f8fafc' }}
                                        itemStyle={{ color: '#f8fafc' }}
                                    />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                    
                    {/* Middle Charts Grid (2 charts) */}
                    <div className="charts-grid" style={{ gridTemplateColumns: 'repeat(2, 1fr)', gridTemplateRows: '280px' }}>
                        
                        {/* Bar Chart: Device Types */}
                        <div className="chart-container">
                            <div className="chart-header">Hardware Footprint</div>
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={deviceData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <XAxis dataKey="name" stroke="#94a3b8" fontSize={10} tickLine={false} />
                                    <YAxis stroke="#94a3b8" fontSize={10} tickLine={false} axisLine={false} />
                                    <RechartsTooltip 
                                        cursor={{ fill: 'rgba(255,255,255,0.05)' }}
                                        contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', color: '#f8fafc' }}
                                    />
                                    <Bar dataKey="count" fill="#3b82f6" radius={[2, 2, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Bar Chart: Event Types */}
                        <div className="chart-container">
                            <div className="chart-header">Event Trigger Analysis</div>
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={eventTypeData} layout="vertical" margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
                                    <XAxis type="number" stroke="#94a3b8" fontSize={10} tickLine={false} axisLine={false} />
                                    <YAxis type="category" dataKey="name" stroke="#94a3b8" fontSize={9} width={120} tickLine={false} axisLine={false} />
                                    <RechartsTooltip 
                                        cursor={{ fill: 'rgba(255,255,255,0.05)' }}
                                        contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', color: '#f8fafc' }}
                                    />
                                    <Bar dataKey="count" fill="#8b5cf6" radius={[0, 4, 4, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>

                    </div>

                    {/* Bottom Table Row */}
                    <div className="table-container">
                        <div className="chart-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span>Asset Inventory</span>
                            <div className="filter-group">
                                <label style={{ marginRight: '10px', fontSize: '10px', color: '#94a3b8' }}>FILTER BY STATUS:</label>
                                <select 
                                    value={statusFilter} 
                                    onChange={(e) => setStatusFilter(e.target.value)}
                                    style={{ 
                                        background: 'rgba(15, 23, 42, 0.8)', 
                                        color: '#f8fafc', 
                                        border: '1px solid #334155', 
                                        padding: '4px 8px',
                                        borderRadius: '4px',
                                        fontFamily: 'inherit',
                                        outline: 'none'
                                    }}
                                >
                                    <option value="ALL">All Assets</option>
                                    <option value="STABLE">Stable</option>
                                    <option value="WARNING">Warning</option>
                                    <option value="THREAT">Threat</option>
                                </select>
                            </div>
                        </div>
                        <div style={{ overflowX: 'auto' }}>
                            <table className="ot-table">
                                <thead>
                                    <tr>
                                        <th>MAC Address</th>
                                        <th>IP Address</th>
                                        <th>Device Type</th>
                                        <th>Protocol</th>
                                        <th>Status</th>
                                        <th>Risk Score</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredAssets.map((asset) => (
                                        <tr key={asset.assetId} onClick={() => setSelectedAsset(asset)}>
                                            <td>{asset.macAddress || asset.assetId.substring(0,8)}</td>
                                            <td>{asset.ipAddress || 'UNKNOWN'}</td>
                                            <td>{asset.deviceType || 'N/A'}</td>
                                            <td>{asset.protocol || 'UNKNOWN'}</td>
                                            <td>
                                                <span className={`status-tag ${asset.riskScore > 70 ? 'danger' : (asset.riskScore > 30 ? 'warning' : 'safe')}`}>
                                                    {asset.riskScore > 70 ? 'THREAT' : (asset.riskScore > 30 ? 'WARNING' : 'STABLE')}
                                                </span>
                                            </td>
                                            <td>
                                                <div className="risk-bar-container">
                                                    <div className="risk-bar-fill" style={{ 
                                                        width: `${asset.riskScore}%`, 
                                                        backgroundColor: asset.riskScore > 70 ? '#ef4444' : (asset.riskScore > 30 ? '#f59e0b' : '#10b981')
                                                    }}></div>
                                                </div>
                                                {asset.riskScore}%
                                            </td>
                                        </tr>
                                    ))}
                                    {filteredAssets.length === 0 && (
                                        <tr>
                                            <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: '#94a3b8' }}>No assets match this filter.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Sidebar */}
                <aside className="dashboard-sidebar">
                    <AlertFeed />
                </aside>
            </main>

            {/* Asset Details Modal */}
            {selectedAsset && (
                <div className="modal-overlay" onClick={() => setSelectedAsset(null)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Asset Identity: {selectedAsset.assetId}</h2>
                            <button className="close-btn" onClick={() => setSelectedAsset(null)}>&times;</button>
                        </div>
                        <div className="modal-body">
                            <div className="detail-grid">
                                <div className="detail-item">
                                    <span className="detail-label">MAC Address</span>
                                    <span className="detail-value">{selectedAsset.macAddress || 'N/A'}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">IP Address</span>
                                    <span className="detail-value">{selectedAsset.ipAddress || 'N/A'}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">First Seen</span>
                                    <span className="detail-value">{new Date(selectedAsset.firstSeen).toLocaleString()}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Last Seen</span>
                                    <span className="detail-value">{new Date(selectedAsset.lastSeen).toLocaleString()}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Ports Used</span>
                                    <span className="detail-value">{selectedAsset.portsUsed && selectedAsset.portsUsed.length > 0 ? selectedAsset.portsUsed.join(', ') : 'None'}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Communication Count</span>
                                    <span className="detail-value">{selectedAsset.communicationCount}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Risk Level</span>
                                    <span className="detail-value" style={{ color: selectedAsset.riskScore > 70 ? '#ef4444' : '#10b981' }}>{selectedAsset.riskLevel} ({selectedAsset.riskScore}%)</span>
                                </div>
                                <div className="detail-item">
                                    <span className="detail-label">Status</span>
                                    <span className="detail-value">{selectedAsset.status}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AnalystDashboard;