package com.sr.Event_Ingestion_Service.service;

import org.springframework.http.HttpEntity;

import com.sr.Event_Ingestion_Service.model.AssetDTO;

public interface ResilientAssetClient {
    
    // public void sendUpdate(UpdateRiskRequest request);

    // Fallback must match the parameter (UpdateRiskRequest)
    // public void handleRetryFailure(UpdateRiskRequest request, Exception e);

    // public void fallbackCircuitBreaker(UpdateRiskRequest request, CallNotPermittedException e);

    public HttpEntity<AssetDTO> checkAssetId(Long assetId);
}
