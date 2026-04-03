package com.sr.RISK_ENGINE.Service.Impl;

import org.springframework.stereotype.Component;

import com.sr.RISK_ENGINE.Client.AssetServiceClient;
import com.sr.RISK_ENGINE.Service.ResilientAssetClient;
import com.sr.RISK_ENGINE.dto.UpdateRiskRequest;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientAssetClientImpl implements ResilientAssetClient {

    private final AssetServiceClient feignClient;

    @Override
    @Retry(name = "assetService", fallbackMethod = "handleRetryFailure")
    @CircuitBreaker(name = "assetService", fallbackMethod = "fallbackCircuitBreaker")
    public void sendUpdate(UpdateRiskRequest request) {
        log.info("!!! RESILIENCE CHECK: Attempting Feign Call for Asset {}", request.assetId());
        feignClient.updateRiskScore(request);
        log.info("Risk Score updated successfully");
    }

    @Override
    public void handleRetryFailure(UpdateRiskRequest request, Exception e) {
        log.error("RETRIES EXHAUSTED for Asset ID: {}. Final Score: {}. Error: {}",
                request.assetId(), request.newRiskScore(), e.getMessage());
    }

    @Override
    public void fallbackCircuitBreaker(UpdateRiskRequest request, CallNotPermittedException e) {
        log.warn("CIRCUIT OPEN: Skipping call for Asset {}. Service is down.", request.assetId());
    }
}