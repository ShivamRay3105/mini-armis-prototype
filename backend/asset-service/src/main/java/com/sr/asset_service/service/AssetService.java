package com.sr.asset_service.service;

import java.util.List;
import java.util.UUID;

import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.model.Asset;

public interface AssetService {

    Asset createAsset(AssetRequest request);

    List<Asset> getAllAssets();

    Asset getAsset(UUID id);

    Asset updateAssetStatus(UUID id, StatusRequest request);

    // List<Asset> getAssetsByStatus(Status status);

    Asset updateAssetLastSeen(UUID id);

    void updateRisk(UUID assetId, double newRiskScore);

    void deleteAsset(UUID id);

    Asset upsertFromEvent(String mac, String ip, Integer port, String protocol, String deviceType);

}