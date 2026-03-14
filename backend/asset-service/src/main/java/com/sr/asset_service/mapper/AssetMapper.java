package com.sr.asset_service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.model.Status;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = Status.class)
public interface AssetMapper {

    // CreateAssetRequest toCreateAssetRequest(Asset asset);
    // 1. CreateAssetRequest → Asset
    @Mapping(target = "riskScore", constant = "0") // default 0
    @Mapping(target = "status", expression = "java(Status.ACTIVE)") // default ACTIVE
    @Mapping(target = "lastSeen", ignore = true)
    @Mapping(target = "id", ignore = true) // ID DB generate
    // @BeanMapping(IgnoreByDefault = true) // Only map fields which are mentioned
    // above , else all don't map
    Asset mapAssetRequestToAssetEntity(AssetRequest request);

    AssetResponse mapAssetEntityToAssetResponse(Asset asset);

    // Bonus: Agar update karna ho future mein (patch/put)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AssetRequest request, @MappingTarget Asset asset);
}
