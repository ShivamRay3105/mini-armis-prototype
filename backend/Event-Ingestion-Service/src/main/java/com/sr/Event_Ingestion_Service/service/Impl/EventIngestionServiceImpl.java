package com.sr.Event_Ingestion_Service.service.Impl;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.sr.Event_Ingestion_Service.Producer.AssetEventProducer;
import com.sr.Event_Ingestion_Service.client.AssetServiceClient;
import com.sr.Event_Ingestion_Service.dto.AssetUpsertRequest;
import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.exception.AssetDecommissionedException;
import com.sr.Event_Ingestion_Service.exception.AssetNotFoundException;
import com.sr.Event_Ingestion_Service.model.AssetDTO;
import com.sr.Event_Ingestion_Service.model.AssetEvent;
import com.sr.Event_Ingestion_Service.model.Status;
import com.sr.Event_Ingestion_Service.service.EventIngestionService;
import com.sr.Event_Ingestion_Service.service.ResilientAssetClient;
import com.sr.Event_Ingestion_Service.util.AssetEventMapper;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionServiceImpl implements EventIngestionService {

    private final AssetEventMapper assetEventMapper;

    private final AssetServiceClient assetServiceClient;

    private final AssetEventProducer assetEventProducer;

    private final ResilientAssetClient resilientAssetClient;

    @Override
    public AssetDTO validateAsset(EventRequest request) {
        UUID assetId = request.assetId();
        
        // If the firewall log doesn't provide an assetId, bypass validation and go straight to discovery/upsert using MAC or IP.
        if (assetId == null) {
            log.info("No assetId provided in event. Falling back to auto-discovery via MAC/IP...");
            return attemptDiscovery(request);
        }

        try {
            log.info("Validating asset with id {}", assetId);
            ResponseEntity<AssetDTO> responseEntity = resilientAssetClient.checkAssetId(assetId);

            if (responseEntity == null) {
                log.error("Asset Service call returned null for id {}", assetId);
                throw new AssetNotFoundException("Asset verification failed: Internal Error");
            }

            // Handle 404 returned as ResponseEntity (if Feign is configured to decode 404s)
            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                return attemptDiscovery(request);
            }

            if (responseEntity.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("Asset Service is currently unavailable for id {}", assetId);
                throw new AssetNotFoundException("Asset verification failed: Service Unavailable");
            }

            AssetDTO asset = responseEntity.getBody();

            if (asset == null) {
                log.warn("Asset not found for id {}. Attempting discovery...", assetId);
                return attemptDiscovery(request);
            }

            if (asset.status() == Status.DECOMMISSIONED) {
                log.warn("Asset is decommissioned for id {}", assetId);
                throw new AssetDecommissionedException("Asset is decommissioned");
            }

            try {
                // We use upsertAsset to ensure lastSeen, communicationCount, and portsUsed are all correctly updated!
                AssetUpsertRequest upsertRequest = new AssetUpsertRequest(
                        asset.macAddress(),
                        asset.ipAddress(),
                        request.port(),
                        request.protocol() != null ? request.protocol() : asset.protocol(),
                        asset.deviceType() != null ? asset.deviceType() : null
                );
                assetServiceClient.upsertAsset(upsertRequest);
                log.info("Successfully updated asset activity state via upsert");
            } catch (Exception e) {
                log.warn("Failed to update asset state : {}", e.getMessage());
            }
            log.info("Asset details: {}", asset);
            return asset;

        } catch (FeignException.NotFound ex) {
            log.warn("Feign 404: Asset not found with ID {}. Attempting discovery...", assetId);
            return attemptDiscovery(request);
        }
    }

    private AssetDTO attemptDiscovery(EventRequest request) {
        UUID assetId = request.assetId();
        if (request.macAddress() != null && !request.macAddress().isBlank()) {
            log.info("MAC Address found: {}. Performing auto-discovery...", request.macAddress());

            AssetUpsertRequest upsertRequest = new AssetUpsertRequest(
                    request.macAddress(),
                    null, // IP not available in EventRequest
                    request.port(),
                    request.protocol(), // Pass the protocol if available
                    null // DeviceType unknown
            );

            try {
                ResponseEntity<AssetDTO> upsertResponse = assetServiceClient.upsertAsset(upsertRequest);
                if (upsertResponse != null && upsertResponse.getBody() != null) {
                    AssetDTO discoveredAsset = upsertResponse.getBody();
                    log.info("Asset discovered/updated successfully: {}", discoveredAsset.assetId());
                    return discoveredAsset;
                }
            } catch (Exception e) {
                log.error("Auto-discovery failed for MAC {}: {}", request.macAddress(), e.getMessage());
            }
        }

        throw new AssetNotFoundException("Asset not found and discovery failed for id " + assetId);
    }

    @Override
    public AssetEvent ingestEvent(EventRequest eventRequest) {

        MDC.put("assetId", String.valueOf(eventRequest.assetId()));

        try {

            log.info("Ingesting event for assetId {}", eventRequest.assetId());
            AssetEvent event = assetEventMapper.mapEventRequestToAssetEvent(eventRequest,
                    validateAsset(eventRequest));
            // here above, asset is passed through with validate function
            log.info("Event created with id {}", event.eventId());
            assetEventProducer.sendEvent(event);
            log.info("Event sent to Kafka topic");
            return event;
        } finally {
            MDC.clear();
        }

    }
}
