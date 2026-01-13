package com.sr.asset_service.service;

import java.util.List;

import com.sr.asset_service.dto.CreateAssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.model.Asset;

public interface AssetService {

    Asset createAsset(CreateAssetRequest request);

    List<Asset> getAllAssets();

    Asset getAsset(Long id);

    Asset updateAssetStatus(Long id, StatusRequest request);

    // List<Asset> getAssetsByStatus(Status status);

    Asset updateAssetLastSeen(Long id);

}