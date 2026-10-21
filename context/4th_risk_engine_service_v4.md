I am building a distributed OT security simulation platform inspired by Armis/Nozomi using Spring Boot microservices and Kafka.

Current system includes:

* Asset Service (stores assets and riskScore)
* Event Ingestion Service (publishes AssetEvent to Kafka)
* Risk Engine Service (stateful Kafka consumer with per-asset context)

Current Risk Engine design:

* Uses ConcurrentHashMap<Long, AssetContext> as state store
* Each AssetContext maintains:

  * riskScore
  * Deque<AssetEvent> (sliding window of 5 minutes)
  * lastUpdated timestamp

Risk calculation pipeline:

* Multiple independent RiskRule implementations
* Each rule evaluates AssetContext and returns a score
* Scores are aggregated (additive model)
* Final score is capped at 100 and decay is applied

Rules implemented so far:

1. BaseScoreRule

* Assigns base weights to events (AUTH_FAILURE, UNAUTHORIZED_ACCESS, PRIVILEGE_ESCALATION, etc.)

2. Frequency Rules

* AuthFailureFrequencyRule
* PortScanFrequencyRule (event-time based, last 60s window)

3. Sequence Rules

* AuthSequenceRule (AUTH_* → UNAUTHORIZED_ACCESS)
* PrivilegeEscalationRule (UNAUTHORIZED_ACCESS / AUTH_SUCCESS → PRIVILEGE_ESCALATION)

4. Correlation Rules

* PortScanToAuthFailureRule (PORT_SCAN → AUTH_FAILURE within 60s, stronger if ≤30s)

Design principles followed:

* Trigger on last event in sequence
* Backward scan with time window
* Per-event localScore → aggregated via max
* Event-time based processing (not Instant.now)
* Clean separation: Service = orchestration, Rules = logic

Event types available:
AUTH_SUCCESS
AUTH_FAILURE
CONNECTION_ATTEMPT
CONFIG_CHANGE
DEVICE_RESTART
PROTOCOL_VIOLATION
MALWARE_DETECTED
UNAUTHORIZED_ACCESS
PRIVILEGE_ESCALATION
PORT_SCAN
SUSPICIOUS_TRAFFIC
DOS_ATTACK
FIRMWARE_CHANGE
CONFIG_TAMPERING
ANOMALOUS_BEHAVIOR
HEARTBEAT_MISSED

Goal for next step:

I want to design and implement additional realistic correlation and sequence rules for remaining event types, such as:

* PORT_SCAN → PROTOCOL_VIOLATION
* PROTOCOL_VIOLATION → SUSPICIOUS_TRAFFIC
* CONFIG_CHANGE → DEVICE_RESTART → ANOMALOUS_BEHAVIOR
* MALWARE_DETECTED → PRIVILEGE_ESCALATION
* DOS_ATTACK patterns
* HEARTBEAT_MISSED sequences

Constraints:

* Keep system simple (solo developer, V1)
* Avoid overengineering
* Maintain consistency with existing rule pattern
* Avoid duplicate logic and excessive coupling
* Keep rules modular and scalable

What I need:

* Help identifying realistic attack chains from these event types
* Prioritization of which rules to implement first
* Clear reasoning for each rule (why it matters)
* Step-by-step guidance (not full code dump)
* Challenge my assumptions and prevent bad design decisions

Also evaluate my backend/system design level as we proceed.
