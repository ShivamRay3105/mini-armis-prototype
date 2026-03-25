package com.sr.asset_service.controller;

import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.dto.UpdateRiskRequest;
import com.sr.asset_service.mapper.AssetMapper;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.service.AssetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
public class AssetController {

        private final AssetService assetService;

        private final AssetMapper assetMapper;

        // CREATE ASSET
        @PostMapping
        public ResponseEntity<AssetResponse> createAsset(
                        @Valid @RequestBody AssetRequest request) {

                Asset createdAsset = assetService.createAsset(request);

                URI location = ServletUriComponentsBuilder
                                .fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(createdAsset.getAssetId())
                                .toUri();

                return ResponseEntity.created(location)
                                .body(assetMapper.mapAssetEntityToAssetResponse(createdAsset));
        }

        // GET ALL ASSETS
        @GetMapping
        public ResponseEntity<List<AssetResponse>> getAllAssets() {

                List<AssetResponse> response = assetService.getAllAssets()
                                .stream()
                                .map(assetMapper::mapAssetEntityToAssetResponse)
                                .toList();

                return ResponseEntity.ok(response);
        }

        // GET ASSET BY ID
        @GetMapping("/{id}")
        public ResponseEntity<AssetResponse> getAssetById(@PathVariable Long id) {

                Asset asset = assetService.getAsset(id);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // UPDATE STATUS
        @PatchMapping("/{id}/status")
        public ResponseEntity<AssetResponse> updateAssetStatus(
                        @PathVariable Long id,
                        @Valid @RequestBody StatusRequest request) {

                Asset asset = assetService.updateAssetStatus(id, request);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // UPDATE LAST SEEN (machine-to-machine)
        @PatchMapping("/{id}/last-seen")
        public ResponseEntity<AssetResponse> updateLastSeen(
                        @PathVariable Long id) {

                Asset asset = assetService.updateAssetLastSeen(id);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // // -------------------------
        // // // UPDATE ASSET RISK SCORE
        // // // (CALLED BY RISK ENGINE)
        // // // -------------------------
        @PutMapping("/update-risk")
        public ResponseEntity<String> updateRiskScore(
                        @RequestBody UpdateRiskRequest request) {

                boolean riskUpdated = assetService.updateRisk(request.assetId(), request.newRiskScore());

                if (riskUpdated) {
                        return ResponseEntity.ok("Risk score updated successfully");
                } else {
                        return ResponseEntity.status(500).body("Failed to update risk score");
                }
        }

}
