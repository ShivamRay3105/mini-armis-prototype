package com.sr.asset_service.service;

import java.util.List;

import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.model.Asset;

public interface AssetService {

    Asset createAsset(AssetRequest request);

    List<Asset> getAllAssets();

    Asset getAsset(Long id);

    Asset updateAssetStatus(Long id, StatusRequest request);

    // List<Asset> getAssetsByStatus(Status status);

    Asset updateAssetLastSeen(Long id);

    void updateRisk(Long assetId, double newRiskScore);

}