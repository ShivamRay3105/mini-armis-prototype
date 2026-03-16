package com.sr.Event_Ingestion_Service.service;

import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.model.AssetEvent;

public interface EventIngestionService {

    void validateAsset(Long assetId);

    AssetEvent ingestEvent(EventRequest eventRequest);

}
