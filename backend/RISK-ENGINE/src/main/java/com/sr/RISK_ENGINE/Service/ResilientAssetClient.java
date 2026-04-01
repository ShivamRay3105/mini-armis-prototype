package com.sr.RISK_ENGINE.Service;

import com.sr.RISK_ENGINE.dto.UpdateRiskRequest;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public interface ResilientAssetClient {

    public void sendUpdate(UpdateRiskRequest request);

    // Fallback must match the parameter (UpdateRiskRequest)
    public void handleRetryFailure(UpdateRiskRequest request, Exception e);

    public void fallbackCircuitBreaker(UpdateRiskRequest request, CallNotPermittedException e);
}