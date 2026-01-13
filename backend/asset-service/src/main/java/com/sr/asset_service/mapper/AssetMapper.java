package com.sr.asset_service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.dto.CreateAssetRequest;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.model.Status;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = Status.class)
public interface AssetMapper {

    // CreateAssetRequest toCreateAssetRequest(Asset asset);
    // 1. CreateAssetRequest → Asset (request se entity banayega)
    @Mapping(target = "riskScore", constant = "0") // default 0 daal
    @Mapping(target = "status", expression = "java(Status.ACTIVE)") // default ACTIVE
    @Mapping(target = "lastSeen", ignore = true) // null hi rahega ya service mein set kar
    @Mapping(target = "id", ignore = true) // ID DB generate karega
    Asset toAssetEntity(CreateAssetRequest request);

    AssetResponse mapAssetEntityToAssetResponse(Asset asset);

    // Bonus: Agar update karna ho future mein (patch/put)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CreateAssetRequest request, @MappingTarget Asset asset);
}
