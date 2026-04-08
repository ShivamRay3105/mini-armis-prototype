import { useState } from 'react';
import useAxiosPrivate from "../../hooks/useAxiosPrivate";

const AssetIngestion = () => {
    const axiosPrivate = useAxiosPrivate();
    const [status, setStatus] = useState('');

    const sendEvent = async (type) => {
        try {
            setStatus(`Transmitting ${type}...`);
            // Hits the Event Ingestion Service -> Kafka -> Risk Engine -> Alert Service
            await axiosPrivate.post('/api/events', {
                type: type,
                plcId: "PLC-SIEMENS-01",
                register: "40001",
                value: type === "PORT_SCAN" ? "REJECTED" : "UNAUTHORIZED",
                timestamp: new Date().toISOString()
            });
            setStatus(`Successfully emitted ${type} to Kafka`);
        } catch (err) {
            setStatus('Hardware Bridge Link Failure');
        }
    };

    return (
        <section className="simulator-container">
            <h2>Hardware Bridge Simulator</h2>
            <p className="role-badge">Active Role: OT_ADMINISTRATOR</p>
            
            <div className="button-group">
                <button onClick={() => sendEvent('PORT_SCAN')} className="btn-warn">
                    Simulate Port Scan (Reg: 40001)
                </button>
                <button onClick={() => sendEvent('AUTH_FAILURE')} className="btn-danger">
                    Simulate Auth Failure (Reg: 40005)
                </button>
            </div>
            
            {status && <p className="status-log">{status}</p>}
        </section>
    );
};

export default AssetIngestion;