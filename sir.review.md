# OT Security Monitoring Platform – Academic Project Explanation

## Purpose of This Project

This project is an **Operational Technology (OT) Security Monitoring System** designed to demonstrate how modern industrial security platforms analyze and monitor OT environments.

The focus is on **backend architecture, system design, and security analytics**, not on hardware programming or PLC configuration.

---

## What This Project DOES

- Models OT environments (PLC, HMI, RTU, SCADA assets)
- Processes OT asset metadata
- Evaluates security risk based on:
  - Protocol characteristics
  - Device criticality
  - Network zone
- Generates alerts for high-risk assets
- Displays real-time security posture on a dashboard

---

## What This Project DOES NOT Do

- No physical PLC or SCADA hardware required
- No packet sniffing or deep protocol parsing
- No exploit detection or malware classification
- No PLC ladder logic or SCADA screen design

These components are **out of scope** and intentionally abstracted.

---

## Role of “Simulation” in This Project

In real OT environments, device discovery is performed by **network sensors** that analyze traffic and output structured asset data.

This project **simulates the output of such sensors**, allowing the system to focus on:

- Asset management
- Risk evaluation
- Alerting logic
- Microservices communication

Simulation represents **realistic upstream data**, not fake logic.

---

## Services and Their Responsibilities

### 1. API Gateway
- Entry point for all requests
- Authentication enforcement
- Request routing

---

### 2. Auth Service
- User authentication
- JWT & refresh token management
- Session security

---

### 3. Event Ingestion Service
- Accepts OT asset events (simulated sensor output)
- Validates asset metadata
- Forwards events internally
- Publishes events to Kafka

**Input Example:**
```json
{
  "deviceType": "PLC",
  "protocol": "MODBUS",
  "zone": "OT"
}
```

4. Asset Service

Maintains OT asset inventory

Stores device type, protocol, and risk score

Acts as source of truth for assets

5. Risk Engine Service

Applies rule-based risk logic

Evaluates protocol insecurity

Considers device criticality

Updates asset risk scores

6. Alert Service

Generates alerts when risk exceeds thresholds

Pushes alerts to dashboard in real time

Maintains alert history

How Risk Is Determined

Risk is based on engineering knowledge, not attack datasets.

Examples:

Legacy protocols without authentication increase risk

Critical devices (PLCs) carry higher impact

OT zone exposure increases severity

This aligns with real-world OT security practices.

Academic & Industry Relevance

This project demonstrates:

Distributed systems

Microservices architecture

Event-driven design

OT security concepts

Backend engineering practices

It is aligned with backend engineering and platform roles, not IC hardware roles.


