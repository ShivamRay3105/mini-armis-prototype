"I am building SentinelStream, a Distributed OT Security Monitoring system following the Purdue Model.

Current Progress:

Asset Service: Manages Level 1 assets with strict IP validation and provides a REST API for risk updates.

Event Ingestion Service: Acts as the Level 2/3 gateway, validating incoming hardware events (via MapStruct) and producing to Kafka.

Risk Engine: A stateful consumer using Redis for AssetContext storage. It implements Sliding Window logic, Behavioral Rule Chaining (Recon -> Access -> Escalation), and Temporal Decay (0.1/sec).

Resilience: Fully implemented Resilience4j (Retry/Circuit Breaker) for inter-service calls and JSON Serialization for Kafka.

Observability: End-to-end MDC Tracing is active across all services.

Next Objective: Build the Alert Service (Kafka Consumer + Postgres + WebSockets) and the Admin Dashboard to visualize real-time attacks from Raspberry Pi hardware."