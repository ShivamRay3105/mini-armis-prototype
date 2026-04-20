package com.sr.asset_service.controller;

import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.dto.AssetUpsertRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.dto.UpdateRiskRequest;
import com.sr.asset_service.mapper.AssetMapper;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.service.AssetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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
        public ResponseEntity<AssetResponse> getAssetById(@PathVariable UUID id) {

                Asset asset = assetService.getAsset(id);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // UPDATE STATUS
        @PatchMapping("/{id}/status")
        public ResponseEntity<AssetResponse> updateAssetStatus(
                        @PathVariable UUID id,
                        @Valid @RequestBody StatusRequest request) {

                Asset asset = assetService.updateAssetStatus(id, request);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // UPDATE LAST SEEN (machine-to-machine)
        @PatchMapping("/{id}/last-seen")
        public ResponseEntity<AssetResponse> updateLastSeen(
                        @PathVariable UUID id) {

                Asset asset = assetService.updateAssetLastSeen(id);
                return ResponseEntity.ok(
                                assetMapper.mapAssetEntityToAssetResponse(asset));
        }

        // // -------------------------
        // // // UPDATE ASSET RISK SCORE
        // // // (CALLED BY RISK ENGINE)
        // // // -------------------------
        @PutMapping("/update-risk")
        public ResponseEntity<Void> updateRiskScore(
                        @RequestBody UpdateRiskRequest request) {

                assetService.updateRisk(request.assetId(), request.newRiskScore());
                // .build() returns no body, which Feign loves for 'void' methods
                return ResponseEntity.ok().build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAsset(@PathVariable UUID id) {
                assetService.deleteAsset(id);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/internal/upsert")
        public ResponseEntity<Void> upsertAsset(@RequestBody AssetUpsertRequest request) {
                assetService.upsertFromEvent(request.macAddress(), request.ipAddress(), request.port(),
                                request.protocol(), request.deviceType());
                return ResponseEntity.ok().build();
        }
}
