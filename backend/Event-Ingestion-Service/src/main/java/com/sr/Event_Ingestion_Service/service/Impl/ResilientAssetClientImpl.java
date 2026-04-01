package com.sr.Event_Ingestion_Service.service.Impl;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import com.sr.Event_Ingestion_Service.client.AssetServiceClient;
import com.sr.Event_Ingestion_Service.model.AssetDTO;
import com.sr.Event_Ingestion_Service.service.ResilientAssetClient;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientAssetClientImpl implements ResilientAssetClient {

    private final AssetServiceClient assetServiceClient;

    // @Override
    // @Retry(name = "assetService", fallbackMethod = "handleRetryFailure")
    // @CircuitBreaker(name = "assetService", fallbackMethod =
    // "fallbackCircuitBreaker")
    // public void sendUpdate(UpdateRiskRequest request) {
    // log.info("!!! RESILIENCE CHECK: Attempting Feign Call for Asset {}",
    // request.assetId());
    // feignClient.updateRiskScore(request);
    // }

    // @Override
    // public void handleRetryFailure(UpdateRiskRequest request, Exception e) {
    // log.error("RETRIES EXHAUSTED for Asset ID: {}. Final Score: {}. Error: {}",
    // request.assetId(), request.newRiskScore(), e.getMessage());
    // }

    // @Override
    // public void fallbackCircuitBreaker(UpdateRiskRequest request,
    // CallNotPermittedException e) {
    // log.warn("CIRCUIT OPEN: Skipping call for Asset {}. Service is down.",
    // request.assetId());
    // }
    // }

    @Override
    @Retry(name = "assetService", fallbackMethod = "handleRetryFailure")
    @CircuitBreaker(name = "assetService", fallbackMethod = "fallbackCircuitBreaker")
    public HttpEntity<AssetDTO> checkAssetId(Long assetId) {
        log.info("!!! RESILIENCE CHECK: Attempting Calling the Asset Service for Asset {}", assetId);
        return assetServiceClient.getAssetById(assetId);
    }

    public HttpEntity<AssetDTO> handleRetryFailure(Long assetId, Exception e) {
        log.error("RETRIES EXHAUSTED for Asset ID: {}. Error: {}", assetId, e.getMessage());
        return new HttpEntity<>(null);
    }

    public HttpEntity<AssetDTO> fallbackCircuitBreaker(Long assetId, CallNotPermittedException e) {
        log.warn("CIRCUIT OPEN: Skipping call for Asset {}. Asset Service is down.", assetId);
        return new HttpEntity<>(null);
    }
}
