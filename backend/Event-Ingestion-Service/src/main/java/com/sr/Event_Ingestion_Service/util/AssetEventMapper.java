package com.sr.Event_Ingestion_Service.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.model.AssetEvent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssetEventMapper {

    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "version", constant = "v1")
    @Mapping(target = "ingestedAt", expression = "java(java.time.Instant.now())")
    @Mapping(source = "assetId", target = "assetId")
    @Mapping(source = "eventType", target = "eventType")
    @Mapping(source = "timestamp", target = "timestamp")
    AssetEvent mapEventRequestToAssetEvent(EventRequest eventRequest);

}
