
### Prompt 1: The Alert Service (The Backend)

> "I am building a microservice called **Alert-Service** as part of a cybersecurity monitoring system named 'SentinelStream'. 
>
> **Goal:** This service must consume risk events from Kafka, persist them for historical auditing, and push them to a frontend via WebSockets.
>
> **Technical Stack:**
> * Spring Boot 3.x, Java 21.
> * **Dependencies:** Spring Web, Spring Data JPA, PostgreSQL, Spring for Apache Kafka, WebSocket, Lombok.
> * **Input:** Kafka topic `risk-alerts`. The message format is a `RiskEvent` JSON (fields: `id`, `assetId`, `riskScore`, `riskLevel`, `reason`, `timestamp`).
>
> **Requirements:**
> 1. Create the `RiskEvent` Entity and Repository.
> 2. Implement a `KafkaListener` to save incoming events to the database.
> 3. Configure a `WebSocketHandler` or `SimpMessagingTemplate` (STOMP) to broadcast these alerts immediately to a topic named `/topic/alerts`.
> 4. Create a REST endpoint `GET /api/alerts/history` to fetch the last 50 alerts.
>
> Please provide the project structure, configuration (`application.yml`), and the Java classes needed to implement this flow."

---

### Prompt 2: The Admin Panel (The UI)

> "I need to build a **SpringBoot Admin Dashboard** for my 'SentinelStream' project.
>
> **Goal:** Create a real-time monitoring page that displays active security threats and allows an administrator to reset 'Locked' assets.
>
> **Requirements:**
> 1. **Live Alert Feed:** Use SockJS and Stomp.js to connect to the Alert Service WebSocket and display incoming alerts in a 'Toast' or a scrolling notification list.
> 2. **Threat Table:** A clean table showing Asset ID, Current Risk Score (color-coded: Red for >80, Yellow for >50), and the latest Reason.
> 3. **The Manual Reset Button:** A button next to each asset that sends a `POST` request to the Risk-Engine service (`/api/risk/reset/{assetId}`) to flip the `requiresManualReset` flag to false.
>
> **Preferred UI Style:** > Please provide a single-page HTML template using **Tailwind CSS** (via CDN) and **Vanilla JavaScript** (or Alpine.js) so I don't have to set up a complex React/Angular environment. I want a 'Cyber/Dark Mode' aesthetic suitable for a government security facility.
>
> Focus on the JavaScript logic to handle the WebSocket connection and the API calls."

---
