package com.sr.Event_Ingestion_Service.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sr.Event_Ingestion_Service.dto.AssetUpsertRequest;
import com.sr.Event_Ingestion_Service.model.AssetDTO;

@FeignClient(name = "ASSET-SERVICE")
public interface AssetServiceClient {

    @GetMapping("/api/v1/assets/{assetId}")
    ResponseEntity<AssetDTO> getAssetById(@PathVariable UUID assetId);

    @PatchMapping("/api/v1/assets/{assetId}/last-seen")
    ResponseEntity<AssetDTO> updateLastSeen(@PathVariable UUID assetId);

    @PostMapping("/api/v1/assets/internal/upsert")
    ResponseEntity<AssetDTO> upsertAsset(@RequestBody AssetUpsertRequest request);
}