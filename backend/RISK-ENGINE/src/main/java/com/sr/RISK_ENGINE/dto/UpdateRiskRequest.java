package com.sr.RISK_ENGINE.dto;

import java.util.UUID;

public record UpdateRiskRequest(UUID assetId, double newRiskScore) {

}
