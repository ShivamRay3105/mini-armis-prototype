package com.sr.RISK_ENGINE.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sr.RISK_ENGINE.dto.AssetUpsertRequest;
import com.sr.RISK_ENGINE.dto.UpdateRiskRequest;

@FeignClient(name = "ASSET-SERVICE")
public interface AssetServiceClient {

    // Existing: called by ResilientAssetClientImpl to push updated risk scores
    @PutMapping("/api/v1/assets/update-risk")
    ResponseEntity<Void> updateRiskScore(@RequestBody UpdateRiskRequest request);

    // CIS-1: called by AssetDiscoveryServiceImpl to auto-create/enrich assets
    @PostMapping("/api/v1/assets/internal/upsert")
    ResponseEntity<Void> upsertAsset(@RequestBody AssetUpsertRequest request);
}
