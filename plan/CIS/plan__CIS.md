Good move. You’re shifting from “reading random things” → **structured execution**. That’s how this project becomes real.

But I’m going to correct one thing first:

> ❌ “I need to complete CIS as part of Armis”
> ✅ “I will map my system features to selected CIS controls”

You’re **not implementing CIS**.
You’re **building features that align with CIS**.

---

# 🧠 Ground Truth — Your System vs CIS

We’ll map **what you already built** vs **what actually matters**.

---

# 🧩 SentinelStream vs CIS Controls (Focused Version)

We’ll only track **relevant controls (5–6 max)** — not all 18.

---

## ✅ CONTROL 1 — Inventory & Control of Assets

### Meaning:

Know every device on network

### Your Status:

* ❌ Automatic discovery → NOT DONE
* ⚠️ Manual asset creation → DONE (but wrong approach)
* ❌ MAC-based identity → NOT DONE
* ❌ Behavioral profiling → NOT DONE

### Verdict:

> 🔴 **CORE GAP (Highest Priority)**

---

## ✅ CONTROL 7 — Vulnerability Management

### Meaning:

Track vulnerabilities + assign risk

### Your Status:

* ✅ Risk score field exists
* ⚠️ Risk logic basic
* ❌ No CVE / vulnerability mapping
* ❌ No dynamic risk updates from behavior

### Verdict:

> 🟡 **PARTIALLY DONE**

---

## ✅ CONTROL 8 — Audit Log Management

### Meaning:

Collect and analyze logs

### Your Status:

* ✅ Kafka ingestion
* ✅ Event pipeline
* ❌ Log persistence strategy unclear
* ❌ No replay / audit querying

### Verdict:

> 🟡 **GOOD FOUNDATION, NEED DEPTH**

---

## ✅ CONTROL 13 — Network Monitoring & Defense

### Meaning:

Monitor traffic + detect anomalies

### Your Status:

* ✅ Risk engine exists
* ⚠️ Basic detection rules
* ❌ No behavior baseline
* ❌ No anomaly deviation logic

### Verdict:

> 🟡 **PARTIAL**

---

## ✅ CONTROL 17 — Incident Response

### Meaning:

React to threats

### Your Status:

* ✅ Alert Service
* ✅ WebSocket live feed
* ❌ No automated response (block/quarantine)
* ❌ No incident lifecycle

### Verdict:

> 🟡 **GOOD BUT SURFACE LEVEL**

---

# 🚫 IGNORE THESE (FOR NOW)

Do NOT waste time on:

* Data Protection
* Account Management
* Email Security
* Training
* Pen Testing

👉 These are **enterprise policies**, not your system’s job.

---

# 🧠 Now — Your TRACKABLE TODO SYSTEM

You said you want a directory-style plan. Good.

---

# 📁 Suggested Project Tracking Structure

```plaintext
sentinelstream-roadmap/
│
├── 01_asset_discovery/
├── 02_risk_engine_enhancement/
├── 03_event_pipeline/
├── 04_alerting_system/
├── 05_observability/
├── 06_testing/
├── 07_documentation/
├── 08_future_ai/
```

---

# 🥇 01 — ASSET DISCOVERY (START HERE)

## Goal:

Convert static asset system → intelligent discovery system

### Tasks:

```plaintext
[ ] Add MAC address to Asset model
[ ] Add firstSeen field
[ ] Add communicationCount
[ ] Add portsUsed tracking

[ ] Build Kafka consumer enrichment layer
[ ] Auto-create asset from events
[ ] Update lastSeen automatically

[ ] Implement basic classification rules:
    - port 502 → PLC
    - port 1883 → IoT (MQTT)

[ ] Store evolving asset profile
```

---

# 🥈 02 — RISK ENGINE ENHANCEMENT

## Goal:

Make risk meaningful (not random score)

### Tasks:

```plaintext
[ ] Add rule-based scoring system
[ ] Add anomaly detection (baseline vs current)

[ ] Implement:
    - same asset high frequency → suspicious
    - new port usage → risk++
    - unusual communication → risk++

[ ] Map detections to MITRE tactics (basic tags)
```

---

# 🥉 03 — EVENT PIPELINE (STRENGTHEN)

```plaintext
[ ] Ensure Kafka key = assetId
[ ] Handle duplicate events
[ ] Add retry / failure handling
[ ] Add dead-letter topic (optional)
```

---

# 🟡 04 — ALERTING SYSTEM

```plaintext
[ ] Add alert severity levels
[ ] Add alert history storage
[ ] Add basic incident grouping
```

---

# 🟡 05 — OBSERVABILITY (Micrometer)

```plaintext
[ ] events_processed_total
[ ] risk_calculation_time
[ ] active_assets
```

---

# 🟡 06 — TESTING

```plaintext
[ ] Build event simulator API
[ ] Test:
    - burst traffic
    - duplicate events
    - service restart
```

---

# 🟡 07 — DOCUMENTATION (VERY IMPORTANT)

```plaintext
[ ] Architecture diagram
[ ] Data flow explanation
[ ] Design decisions
[ ] Tradeoffs (WHY you did things)
```

---

# 🔵 08 — AI (LATER)

```plaintext
[ ] MCP server
[ ] AI analyst queries
```

---

# 🧨 What You Should Do TODAY

Not 10 things.

Just this:

```plaintext
1. Create roadmap directory
2. Start 01_asset_discovery
3. Modify Asset model
```

---

# 🧠 Final Strategic Insight

Right now:

> You are transitioning from
> “CRUD backend developer”
> → “System thinker (OT security)”

That shift happens ONLY if:

* you stop chasing frameworks
* and start building **core intelligence**

---

# 🚀 Next Step

Say:

👉 **“Give me exact Asset model + DB schema + Redis structure”**

We’ll lock your foundation before writing any more logic.


🆕 asset-discovery-engine — New files to create
asset-discovery-engine/
└── src/main/java/com/sr/discovery/
    ├── consumer/
    │   └── NetworkEventConsumer.java   ← Kafka listener
    ├── service/
    │   └── AssetDiscoveryService.java  ← classification + enrichment logic
    ├── client/
    │   └── AssetServiceClient.java     ← Feign client → calls asset-service REST
    └── dto/
        ├── NetworkEvent.java           ← Kafka message shape
        └── DiscoveredAssetRequest.java ← what it sends to asset-service