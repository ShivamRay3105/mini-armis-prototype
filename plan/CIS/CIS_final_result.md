# CIS Compliance Implementation — Final Result

**Branch:** `CIS`  
**Commit:** `3ad442b`  
**Date:** 2026-04-20  
**Services modified:** `RISK-ENGINE`, `Event-Ingestion-Service`, `ALERT-SERVICE`, `asset-service`  
**Services untouched (as instructed):** `API-GATEWAY`, `AUTH-SERVICE`, `Service-Registry`

---

## ✅ Status: COMPLETE

All tasks from `plan_2_CIS.md` are implemented, all 4 services compile with `BUILD SUCCESS` on Java 25.

---

## What Was Built (Control by Control)

---

### CIS Control 1 — Inventory & Control of Assets

**Goal:** Every device that appears on the network should be automatically discovered and registered — not manually entered.

**Approach:** The RISK-ENGINE already consumes every event from the `asset-events` Kafka topic. Instead of adding a new service (which would cost memory, a new Kafka group, and deployment overhead), we piggyback the discovery logic directly inside the RISK-ENGINE's existing listener.

#### Files Created / Modified

| File | Service | Change |
|---|---|---|
| `AssetDiscoveryService.java` | RISK-ENGINE | New interface for CIS-1 discovery |
| `AssetDiscoveryServiceImpl.java` | RISK-ENGINE | Classifies device by port, calls asset-service upsert |
| `AssetUpsertRequest.java` | RISK-ENGINE | New DTO sent to asset-service with `deviceType` field |
| `AssetServiceClient.java` | RISK-ENGINE | Added `upsertAsset()` Feign method |
| `AssetEventsListener.java` | RISK-ENGINE | Calls `discoverAndEnrich()` before risk processing |
| `AssetUpsertRequest.java` | asset-service | Added `deviceType` field |
| `AssetController.java` | asset-service | Passes `deviceType` to service layer |
| `AssetService.java` | asset-service | Updated `upsertFromEvent()` signature |
| `AssetServiceImpl.java` | asset-service | Stores `deviceType` on new and existing assets |

#### Classification Rules (in `AssetDiscoveryServiceImpl`)

| Port | Protocol | Device Type |
|---|---|---|
| 502 | MODBUS | `PLC` — Programmable Logic Controller |
| 1883 | MQTT | `IOT` — IoT sensor/actuator |
| 20000 | DNP3 | `RTU` — Remote Terminal Unit (SCADA) |
| 102 | S7COMM | `PLC` — Siemens PLC |
| 44818 | EtherNet/IP | `PLC` — Allen-Bradley PLC |
| 47808 | BACnet | `BMS` — Building Management System |
| *any* | MODBUS | `PLC` |
| *any* | DNP3 | `RTU` |
| *any* | MQTT | `IOT` |
| unknown | unknown | `UNKNOWN` |

> **Design decision:** Discovery failures are caught and logged inside `AssetDiscoveryServiceImpl` — they NEVER interrupt the risk pipeline. Asset discovery is best-effort; risk scoring is not.

---

### CIS Control 7 — Vulnerability Management
### CIS Control 13 — Network Monitoring & Defense

**Goal:** Risk score should reflect real behavioral anomalies — not just auth failures. OT-specific protocols need special treatment.

#### New Risk Rules

| Rule | File | Score Added | Trigger |
|---|---|---|---|
| `NewPortUsageRule` | `Rules/Network/` | +15 (1 new port) / +25 (2+ new ports) | Asset communicates on a port it has never used before |
| `UnusualProtocolRule` | `Rules/Network/` | +20 (1 occurrence) / +35 (3+) | `SUSPICIOUS_TRAFFIC` on MODBUS, DNP3, S7COMM, PROFINET, BACnet, EIP |

#### Supporting Changes

| File | Service | Change |
|---|---|---|
| `AssetEvent.java` | RISK-ENGINE | Added `Integer port`, `String macAddress` fields |
| `AssetContext.java` | RISK-ENGINE | Added `Set<Integer> knownPorts` + `addPortAndCheckIfNew()` — persisted in Redis |
| `EventType.java` | RISK-ENGINE | Added `NEW_PORT_DETECTED`, `UNUSUAL_PROTOCOL` |
| `RiskServiceImpl.java` | RISK-ENGINE | Registered `NewPortUsageRule` + `UnusualProtocolRule` in the rule chain |

#### MITRE ATT&CK Mapping (in `RiskReasonBuilder`)

Every alert reason string now carries a MITRE tactic tag:

| Event Type | MITRE Tag |
|---|---|
| `UNAUTHORIZED_ACCESS` | `TA0001 - Initial Access` |
| `PRIVILEGE_ESCALATION` | `TA0004 - Privilege Escalation` |
| `AUTH_FAILURE` | `TA0006 - Credential Access` |
| `PORT_SCAN` | `TA0043 - Reconnaissance` |
| `NEW_PORT_DETECTED` | `TA0007 - Discovery` |
| `UNUSUAL_PROTOCOL` | `TA0040 - Impact` |
| `SUSPICIOUS_TRAFFIC` | `TA0011 - Command and Control` |
| `ANOMALOUS_BEHAVIOR` | `TA0002 - Execution` |

---

### CIS Control 8 — Audit Log Management

**Goal:** Every event in the pipeline must be traceable. Delivery failures must be logged. Alerts must be queryable by device and time.

#### Changes

| File | Service | Change |
|---|---|---|
| `AssetEvent.java` | Event-Ingestion-Service | Added `Integer port`, `String macAddress` fields |
| `EventRequest.java` | Event-Ingestion-Service | Added optional `port`, `macAddress` fields |
| `AssetEventMapper.java` | Event-Ingestion-Service | Maps `port` + `macAddress` from request → Kafka event |
| `AssetEventProducer.java` | Event-Ingestion-Service | Added `whenComplete()` callback: logs partition + offset on success, logs error on failure |
| `RiskEvent.java` | ALERT-SERVICE | Added `severity` (enum), `groupKey` (string) columns |
| `AlertConsumerServiceImpl.java` | ALERT-SERVICE | Sets `severity` from `riskScore`, sets `groupKey = assetId:riskLevel` before persisting |
| `RiskEventRepository.java` | ALERT-SERVICE | Added `findByAssetId()` for per-device audit trail |

---

### CIS Control 17 — Incident Response

**Goal:** Alerts should be queryable by priority, grouped into incidents, and support a resolution lifecycle.

#### New Endpoints (ALERT-SERVICE)

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/alerts/history` | GET | Last 50 alerts (existing). Now supports `?severity=HIGH` filter |
| `/api/alerts/active` | GET | All unresolved alerts, newest first — **primary SOC triage queue** |
| `/api/alerts/grouped` | GET | Active alerts grouped by `assetId:riskLevel` — **incident view** |
| `/api/alerts/asset/{uuid}` | GET | All alerts for a specific asset — **per-device audit trail** |
| `/api/alerts/summary` | GET | Count of unresolved alerts per severity level — **dashboard KPIs** |
| `/api/alerts/{id}/resolve` | POST | Mark alert as resolved (existing) |

#### New Files

| File | What it does |
|---|---|
| `AlertSeverity.java` | Enum: `LOW / MEDIUM / HIGH / CRITICAL` with `fromScore(double)` factory |

#### Severity Thresholds

| Score | Severity |
|---|---|
| ≥ 80 | `CRITICAL` |
| ≥ 50 | `HIGH` |
| ≥ 20 | `MEDIUM` |
| < 20 | `LOW` |

---

### Infrastructure Fix

Lombok upgraded from `1.18.32` / `1.18.34` → `1.18.36` across all four services.  
**Why:** Java 25 removed `sun.misc.Unsafe::objectFieldOffset`, which older Lombok versions use via `lombok.permit.Permit`. This was a pre-existing build blocker — all four services now compile cleanly.

---

## How to Test

### Prerequisites

Make sure these are running (via docker-compose or locally):
- Kafka on `localhost:9092`
- PostgreSQL on `localhost:5432` (database: `sentinel-alert`)
- Redis on `localhost:6379`
- Eureka (Service-Registry) running
- `asset-service` running (port 8080)
- `RISK-ENGINE` running (port 8500)
- `ALERT-SERVICE` running (port 8200)
- `Event-Ingestion-Service` running (any port)

---

### Test 1 — CIS-1: Asset Auto-Discovery

**Goal:** Send a network event — asset should be auto-created in the database without manual registration.

```bash
# Step 1: Send an event for a NEW asset (not in DB yet) with a MODBUS port
POST http://localhost:<EIS-port>/api/events/ingest
Content-Type: application/json

{
  "assetId": <any-existing-asset-id-for-validation>,
  "eventType": "SUSPICIOUS_TRAFFIC",
  "timestamp": "2026-04-20T05:00:00Z",
  "port": 502,
  "macAddress": "AA:BB:CC:DD:EE:FF"
}
```

**Expected result:**
- RISK-ENGINE logs: `[CIS-1] Auto-discovery: asset MAC=AA:BB:CC:DD:EE:FF IP=... classified as PLC`
- `GET http://localhost:8080/api/v1/assets` — new asset appears with `deviceType: PLC`

---

### Test 2 — CIS-13: New Port Usage Detection

**Goal:** Send two events for the same asset on different ports — second event on a new port should raise risk score.

```bash
# Event 1: port 502 (establishes baseline)
POST http://localhost:<EIS-port>/api/events/ingest
{ "assetId": <uuid>, "eventType": "AUTH_SUCCESS", "timestamp": "...", "port": 502 }

# Event 2: port 8080 (NEW port — should trigger NewPortUsageRule)
POST http://localhost:<EIS-port>/api/events/ingest
{ "assetId": <same-uuid>, "eventType": "SUSPICIOUS_TRAFFIC", "timestamp": "...", "port": 8080 }
```

**Expected result:**
- RISK-ENGINE logs rule evaluation with `NewPortUsageRule` contributing `+15` to score
- `GET http://localhost:8080/api/v1/assets/<uuid>` shows elevated `riskScore`

---

### Test 3 — CIS-13: OT Protocol Anomaly

**Goal:** Send SUSPICIOUS_TRAFFIC on a MODBUS protocol — should add OT-specific risk.

```bash
POST http://localhost:<EIS-port>/api/events/ingest
{
  "assetId": <uuid>,
  "eventType": "SUSPICIOUS_TRAFFIC",
  "timestamp": "...",
  "port": 502
}
```

**Expected result:**
- `UnusualProtocolRule` fires (asset's `protocol` field is `MODBUS`)
- Risk score increases by `+20`
- If score ≥ 70: ALERT-SERVICE receives Kafka message on `risk-alerts` topic
- Alert reason includes `[MITRE: TA0040 - Impact]`

---

### Test 4 — CIS-8: Kafka Send Audit

**Expected:** On every event sent by EIS, check EIS logs for:
```
[CIS-8] Kafka send SUCCESS: eventId=... → topic=asset-events partition=0 offset=42
```
If Kafka is down:
```
[CIS-8] Kafka send FAILED for eventId=... assetId=...: <error>
```

---

### Test 5 — CIS-17: Incident Response Endpoints

```bash
# Active (unresolved) alerts
GET http://localhost:8200/api/alerts/active

# Grouped by incident key
GET http://localhost:8200/api/alerts/grouped

# Filter history by severity
GET http://localhost:8200/api/alerts/history?severity=HIGH

# Per-device audit trail
GET http://localhost:8200/api/alerts/asset/<asset-uuid>

# Dashboard KPI summary
GET http://localhost:8200/api/alerts/summary
# Expected: { "CRITICAL": 2, "HIGH": 5, "MEDIUM": 8, "LOW": 3 }

# Resolve an alert
POST http://localhost:8200/api/alerts/7/resolve
```

---

### Test 6 — End-to-End MITRE Tag Verification

Trigger a `PORT_SCAN` event and check the saved `reason` field in the `risk_events` table:

```sql
SELECT reason FROM risk_events ORDER BY timestamp DESC LIMIT 1;
-- Expected: "[MITRE: TA0043 - Reconnaissance] Port scanning activity detected on asset ..."
```

---

## What's NOT Done (Intentionally Skipped)

Per the plan, these were explicitly excluded:

| Item | Reason |
|---|---|
| Dead-letter Kafka topic | Optional — adds operational complexity for a prototype |
| CVE / vulnerability database mapping | Enterprise-grade, out of scope for this system |
| Automated block/quarantine response | Requires firewall integration — not in scope |
| Observability (Micrometer metrics) | Next phase (plan section 05) |
| Event simulator API | Next phase (plan section 06) |
| MCP / AI queries | Future phase (plan section 08) |

---

## Data Flow After CIS Changes

```
Firewall / OT Device
        │
        ▼
Event-Ingestion-Service   ← now carries port + macAddress in events
        │
        │  Kafka: asset-events (key = assetId)
        ▼
RISK-ENGINE (Kafka Consumer)
   ├── AssetEventsListener.consume()
   │       │
   │       ├── [CIS-1] AssetDiscoveryService.discoverAndEnrich()
   │       │       └── Feign → asset-service /internal/upsert (classify + enrich)
   │       │
   │       └── [CIS-7/13] RiskService.processEvent()
   │               ├── BaseScoreRule
   │               ├── AuthFailureFrequencyRule
   │               ├── AuthSequenceRule
   │               ├── PrivilegeEscalationRule
   │               ├── PortScanFrequencyRule
   │               ├── NewPortUsageRule  ← NEW (CIS-13)
   │               └── UnusualProtocolRule  ← NEW (CIS-13)
   │
   ├── [CIS-7] Feign → asset-service /update-risk (update score in DB)
   │
   └── [CIS-17] Kafka → risk-alerts (if score ≥ 70 or atomic alert)
                │
                ▼
        ALERT-SERVICE (Kafka Consumer)
            └── Sets severity + groupKey → saves to PostgreSQL
                    └── WebSocket push to frontend /topic/alerts
```
