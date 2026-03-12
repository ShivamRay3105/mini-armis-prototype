package com.sr.asset_service.controller;

import com.sr.asset_service.dto.AssetResponse;
import com.sr.asset_service.dto.CreateAssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.mapper.AssetMapper;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.service.AssetService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetMapper assetMapper;

    // CREATE ASSET
    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(
            @Valid @RequestBody CreateAssetRequest request) {

        Asset createdAsset = assetService.createAsset(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAsset.getId())
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
    // // @PatchMapping("/{id}/risk")
    // // public Asset updateRiskScore(
    // // @PathVariable Long id,
    // // @Valid @RequestBody UpdateRiskRequest request) {

    // // Asset asset = assetRepository.findById(id)
    // // .orElseThrow(() -> new RuntimeException("Asset not found"));

    // // asset.setRiskScore(request.getRiskScore());
    // // return assetRepository.save(asset);
    // // }
}
