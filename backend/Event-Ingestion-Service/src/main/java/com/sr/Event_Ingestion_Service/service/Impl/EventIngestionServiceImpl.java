package com.sr.Event_Ingestion_Service.service.Impl;

import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import com.sr.Event_Ingestion_Service.Producer.AssetEventProducer;
import com.sr.Event_Ingestion_Service.client.AssetServiceClient;
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
    public AssetDTO validateAsset(Long assetId) {

        try {
            log.info("Validating asset with id {}", assetId);
            // AssetDTO asset = assetServiceClient.getAssetById(assetId).getBody();
            // Get the wrapper
            HttpEntity<AssetDTO> responseEntity = resilientAssetClient.checkAssetId(assetId);

            // Safely get the body
            AssetDTO asset = (responseEntity != null) ? responseEntity.getBody() : null;

            if (asset == null) {
                log.warn("Asset not found or service unavailable for id {}", assetId);
                // Throwing this ensures the event isn't ingested if we can't verify the asset
                throw new AssetNotFoundException("Asset verification failed");
            }

            if (asset.status() == Status.DECOMMISSIONED) {
                log.warn("Asset is decommissioned for id {}", assetId);
                throw new AssetDecommissionedException("Asset is decommissioned");
            }

            try {
                assetServiceClient.updateLastSeen(assetId);
            } catch (Exception e) {
                log.warn("Failed to update last seen : {}", e.getMessage());
            }
            log.info("Successfully updated last seen");
            log.info("Asset details: {}", asset);
            return asset;

        } catch (FeignException.NotFound ex) {
            log.warn("Received 404 from asset-service");
            throw new AssetNotFoundException("Asset not found with id " + assetId);
        }
    }

    @Override
    public AssetEvent ingestEvent(EventRequest eventRequest) {

        MDC.put("assetId", String.valueOf(eventRequest.assetId()));

        try {

            log.info("Ingesting event for assetId {}", eventRequest.assetId());
            AssetEvent event = assetEventMapper.mapEventRequestToAssetEvent(eventRequest,
                    validateAsset(eventRequest.assetId()));
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
