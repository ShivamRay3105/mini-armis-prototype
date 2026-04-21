package com.sr.Event_Ingestion_Service.service.Impl;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.sr.Event_Ingestion_Service.client.AssetServiceClient;
import com.sr.Event_Ingestion_Service.model.AssetDTO;
import com.sr.Event_Ingestion_Service.service.ResilientAssetClient;

import feign.FeignException;
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

    @Override
    @Retry(name = "assetService", fallbackMethod = "handleRetryFailure")
    @CircuitBreaker(name = "assetService", fallbackMethod = "fallbackCircuitBreaker")
    public ResponseEntity<AssetDTO> checkAssetId(UUID assetId) {
        log.info("!!! RESILIENCE CHECK: Attempting Calling the Asset Service for Asset {}", assetId);
        return assetServiceClient.getAssetById(assetId);
    }

    public ResponseEntity<AssetDTO> handleRetryFailure(UUID assetId, Exception e) {
        if (e instanceof FeignException.NotFound) {
            log.warn("Retry failed due to 404: Asset {} not found.", assetId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        log.error("RETRIES EXHAUSTED for Asset ID: {}. Error: {}", assetId, e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public ResponseEntity<AssetDTO> fallbackCircuitBreaker(UUID assetId, CallNotPermittedException e) {
        log.warn("CIRCUIT OPEN: Skipping call for Asset {}. Asset Service is down.", assetId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
