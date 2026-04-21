package com.sr.Event_Ingestion_Service.service;

import java.util.UUID;

import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.model.AssetDTO;
import com.sr.Event_Ingestion_Service.model.AssetEvent;

public interface EventIngestionService {

    AssetDTO validateAsset(EventRequest request);

    AssetEvent ingestEvent(EventRequest eventRequest);

}
