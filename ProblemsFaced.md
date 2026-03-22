# Risk Engine Service:
1. While implementing the calculation of risk score of different event types, i started with Access Attempts chain, which includes Auth Success, Auth Failure and Unauthorized Access event types, I implemented it's calculating structure in one 
file only

 private double calculateRisk(AssetContext assetContext) {

        Deque<AssetEvent> events = assetContext.getRecentEvents();

        double score = 0;

        // base score
        score += calculateBaseScore(events);

        // frequency and patterns
        score += detectFrequency(events);

        // sequences
        score += detectSequences(events);

        // (later) decay rules
        score = applyDecay(score, assetContext);

        score = Math.min(score, 100);

        return score;
    }

This inside-used functions for calculating the final score 

🧠 How to Think Going Forward

When adding new logic, don’t ask:

❌ “Which event type?”
✅ “Which pattern?”

### 🎯 Example Mapping

| Pattern                 | Rule Class               |
| ----------------------- | ------------------------ |
| AUTH_FAILURE burst      | AuthFailureFrequencyRule |
| PORT_SCAN burst         | PortScanFrequencyRule    |
| AUTH → UNAUTHORIZED     | AuthSequenceRule         |
| UNAUTHORIZED → PRIV_ESC | PrivilegeEscalationRule  |
| CONFIG → RESTART        | ConfigChangeSequenceRule |
