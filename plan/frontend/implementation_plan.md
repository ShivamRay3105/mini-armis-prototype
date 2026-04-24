# Implementation Plan: Nozomi-Style OT Security Analyst Dashboard

This plan outlines the complete UX/UI overhaul of the Security Analyst Dashboard. The goal is to transform the current interface into a sleek, dark-themed, dynamic dashboard reminiscent of top-tier OT security products (like Nozomi Networks) and the Spring Admin Server.

## Proposed Changes

### 1. Dependencies
#### [NEW] Recharts
I will install the `recharts` library (`npm install recharts`) to provide high-performance, dynamic charts built specifically for React.

### 2. Layout & Architecture Redesign
The dashboard will be split into a rich central data area and a live sidebar.

**Central Area (Left & Center):**
*   **Top Metric Cards:** 4 glowing cards showing critical summaries (Total Assets, Critical Threats, Total Alerts, Average Risk Index).
*   **Visualizations (Middle Row):**
    *   **Risk Distribution (Donut Chart):** Visualizing the proportion of Low, Medium, High, and Critical risk assets.
    *   **Protocol Activity (Bar Chart):** Displaying asset counts by protocol (e.g., MODBUS, DNP3, TCP).
*   **Asset Data Grid (Bottom Row):** A comprehensive, sortable table replacing the current grid of cards. It will display columns: MAC, IP, Device Type, Protocol, Ports, and a dynamic Risk Score bar.
    *   **Asset Popups:** Clicking a row will trigger a glassmorphic modal overlay showing deep details (Communication Count, First/Last Seen, Risk Factors).

**Live Sidebar (Right):**
*   **Incident Response Feed:** The existing `AlertFeed.jsx` will be heavily restyled. Alerts will pulse with a red/orange glow depending on severity, keeping the "live threat" feel.

### 3. Styling & Aesthetics (CSS)
I will completely rewrite `AnalystDashboard.css`.
*   **Color Palette:** Deep dark backgrounds (`#0b1120`), glassmorphic panels (`rgba(30, 41, 59, 0.7)`), and stark neon accents:
    *   Stable/Safe: Neon Green (`#00ff88`)
    *   Warning: Cyber Yellow (`#fbbf24`)
    *   Threat: Alert Red (`#ff4d4d`)
*   **Effects:** Subtle glowing borders on critical alerts, smooth hover transitions on table rows, and entrance animations for data rendering.

### 4. Component Updates

#### [MODIFY] frontend/src/components/Dashboard/AnalystDashboard.jsx
*   Integrate Recharts components (`PieChart`, `BarChart`, `ResponsiveContainer`).
*   Implement data aggregation functions (e.g., counting risk levels, grouping by protocols) based on the data fetched from `/api/v1/assets`.
*   Add a Modal/Popup state for asset deep-dives.

#### [MODIFY] frontend/src/components/Alerts/AlertFeed.jsx
*   Update CSS classes and layout to match the new dark OT aesthetic.

#### [MODIFY] frontend/src/styles/AnalystDashboard.css
*   Complete rewrite applying the new color variables, CSS Grid/Flexbox layouts, glassmorphism, and animations.

## User Review Required

> [!IMPORTANT]
> I will need to stop the frontend dev server, run `npm install recharts`, and restart it. This will briefly interrupt your local `localhost:5173` session.

> [!TIP]
> Does this layout (Metrics -> Charts -> Data Table + Live Sidebar) align with your vision for the Nozomi-style dashboard?

## Verification Plan

### Manual Verification
1.  Verify the dashboard loads without errors.
2.  Verify charts correctly process the mock/live data from the backend.
3.  Click on an asset in the table to ensure the deep-dive popup renders correctly.
4.  Verify the dark-mode aesthetic with green/red glowing accents is applied uniformly.
