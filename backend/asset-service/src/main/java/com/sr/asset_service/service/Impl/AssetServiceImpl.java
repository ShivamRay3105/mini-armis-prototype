package com.sr.asset_service.service.Impl;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sr.asset_service.Exception.DuplicateResourceException;
import com.sr.asset_service.Exception.ResourceNotFoundException;
import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.mapper.AssetMapper;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.repository.AssetRepository;
import com.sr.asset_service.service.AssetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    private final AssetMapper assetMapper;

    @Override
    public Asset createAsset(AssetRequest request) {

        Asset newAsset = assetMapper.mapAssetRequestToAssetEntity(request);
        newAsset.setLastSeen(Instant.now());

        try {
            return assetRepository.save(newAsset);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException(
                    "Asset with IP " + request.ipAddress() + " already exists");
        }
    }

    @Override
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public Asset getAsset(Long id) {
        return assetRepository.findById(id.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));
    }

    @Override
    public Asset updateAssetStatus(Long id, StatusRequest request) {
        Asset asset = assetRepository.findById(id.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));

        asset.setStatus(request.status());
        return assetRepository.save(asset);
    }

    // @Override
    // public List<Asset> getAssetsByStatus(Status status) {
    // // return assetRepository.findByStatus(status);
    // // }

    @Override
    public Asset updateAssetLastSeen(Long id) {
        Asset asset = assetRepository.findById(id.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));

        asset.setLastSeen(Instant.now());
        return assetRepository.save(asset);
    }

    @Override
    @Transactional
    public void updateRisk(Long assetId, double newRiskScore) {
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + assetId));

        asset.setRiskScore((int) Math.round(newRiskScore));
        assetRepository.save(asset);

    }

    @Override
    public void deleteAsset(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("No Asset found with id " + id);
        }
        assetRepository.deleteById(id);
    }

}
