package com.sr.Event_Ingestion_Service.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.model.AssetDTO;
import com.sr.Event_Ingestion_Service.model.AssetEvent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssetEventMapper {

    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "version", constant = "v1")
    @Mapping(target = "ingestedAt", expression = "java(java.time.Instant.now())")
    @Mapping(source = "eventRequest.assetId", target = "assetId", defaultExpression = "java(assetDTO != null ? assetDTO.assetId() : null)")
    @Mapping(source = "eventRequest.eventType", target = "eventType")
    @Mapping(source = "eventRequest.timestamp", target = "timestamp")
    @Mapping(source = "eventRequest.port", target = "port")           // CIS-13
    @Mapping(source = "eventRequest.macAddress", target = "macAddress") // CIS-1
    @Mapping(target = "protocol", expression = "java(eventRequest.protocol() != null ? eventRequest.protocol() : (assetDTO != null ? assetDTO.protocol() : null))")
    @Mapping(source = "assetDTO.ipAddress", target = "ipAddress")
    @Mapping(source = "assetDTO.deviceType", target = "deviceType")
    AssetEvent mapEventRequestToAssetEvent(EventRequest eventRequest, AssetDTO assetDTO);

}
