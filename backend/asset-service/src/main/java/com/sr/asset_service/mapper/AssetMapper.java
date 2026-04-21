package com.sr.asset_service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.sr.asset_service.Enums.RiskLevel;
import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.model.Status;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
        Status.class, RiskLevel.class })
public interface AssetMapper {

    @Mapping(target = "riskScore", constant = "0")
    @Mapping(target = "riskLevel", expression = "java(RiskLevel.LOW)")
    @Mapping(target = "status", expression = "java(Status.ACTIVE)")
    @Mapping(target = "lastSeen", ignore = true)
    @Mapping(target = "firstSeen", ignore = true)
    @Mapping(target = "assetId", ignore = true)
    @Mapping(target = "communicationCount", constant = "0L")
    Asset mapAssetRequestToAssetEntity(AssetRequest request);

    @Mapping(source = "assetId", target = "assetId")
    AssetResponse mapAssetEntityToAssetResponse(Asset asset);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AssetRequest request, @MappingTarget Asset asset);
}
