package com.sr.asset_service.dto;

public record UpdateRiskRequest(Long assetId, double newRiskScore) {

}
