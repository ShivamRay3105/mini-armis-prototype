package com.sr.RISK_ENGINE.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sr.RISK_ENGINE.model.EventType;

public class AssetContext {
    Long assetId;

    int riskScore;

    List<EventType> recentEvents = new ArrayList<>();

    Map<EventType, List<Instant>> eventHistory = new HashMap<>();
}
