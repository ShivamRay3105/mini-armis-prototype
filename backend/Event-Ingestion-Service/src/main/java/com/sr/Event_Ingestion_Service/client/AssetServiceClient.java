package com.sr.Event_Ingestion_Service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sr.Event_Ingestion_Service.model.AssetDTO;

@FeignClient(name = "asset-service")
public interface AssetServiceClient {

    @GetMapping("/api/v1/assets/{assetId}")
    ResponseEntity<AssetDTO> getAssetById(@PathVariable Long assetId);

    @PatchMapping("/api/v1/assets/{assetId}/last-seen")
    ResponseEntity<AssetDTO> updateLastSeen(@PathVariable Long assetId);
}