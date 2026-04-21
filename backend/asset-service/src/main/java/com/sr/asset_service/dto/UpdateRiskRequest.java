package com.sr.asset_service.dto;

import java.util.UUID;

public record UpdateRiskRequest(UUID assetId, double newRiskScore) {

}
