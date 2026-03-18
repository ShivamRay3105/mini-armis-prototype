package com.sr.Event_Ingestion_Service.service.Impl;

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

    @Override
    public AssetDTO validateAsset(Long assetId) {
        log.info("Validating asset with id {}", assetId);
        try {
            AssetDTO asset = assetServiceClient.getAssetById(assetId).getBody();
            log.info("Asset lookup completed. Asset DTO:", asset);

            if (asset == null) {
                log.warn("Asset not found for id {}", assetId);
                throw new AssetNotFoundException("Asset not found with id " + assetId);
            }

            if (asset.status() == Status.DECOMMISSIONED) {
                log.warn("Asset is decommissioned for id {}", assetId);
                throw new AssetDecommissionedException("Asset is decommissioned with id " + assetId);
            }

            assetServiceClient.updateLastSeen(assetId);
            log.info("Successfully updated last seen for asset id {}", assetId);
            log.info("Asset details: {}", asset);
            return asset;

        } catch (FeignException.NotFound ex) {
            log.warn("Received 404 from asset-service for id {}", assetId);
            throw new AssetNotFoundException("Asset not found with id " + assetId);
        }
    }

    @Override
    public AssetEvent ingestEvent(EventRequest eventRequest) {
        log.info("Ingesting event for assetId {}", eventRequest.assetId());
        AssetEvent event = assetEventMapper.mapEventRequestToAssetEvent(eventRequest,
                validateAsset(eventRequest.assetId()));
        // here above, asset is passed through with validate function
        log.info("Event created with id {}", event.eventId());
        assetEventProducer.sendEvent(event);
        log.info("Event sent to Kafka topic for assetId {}", eventRequest.assetId());
        return event;
    }

}
