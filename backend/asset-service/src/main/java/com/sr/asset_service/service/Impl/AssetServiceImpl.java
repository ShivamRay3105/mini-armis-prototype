package com.sr.asset_service.service.Impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sr.asset_service.Enums.RiskLevel;
import com.sr.asset_service.Exception.DuplicateResourceException;
import com.sr.asset_service.Exception.ResourceNotFoundException;
import com.sr.asset_service.dto.AssetRequest;
import com.sr.asset_service.dto.StatusRequest;
import com.sr.asset_service.mapper.AssetMapper;
import com.sr.asset_service.model.Asset;
import com.sr.asset_service.model.Status;
import com.sr.asset_service.repository.AssetRepository;
import com.sr.asset_service.service.AssetCacheService;
import com.sr.asset_service.service.AssetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    private final AssetMapper assetMapper;

    private final AssetCacheService cacheService;

    @Transactional
    public Asset createAsset(AssetRequest request) {
        Asset newAsset = assetMapper.mapAssetRequestToAssetEntity(request);
        newAsset.setLastSeen(Instant.now());

        Asset saved = assetRepository.save(newAsset);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        cacheService.cacheAsset(saved);
                    }
                });

        return saved;
    }

    @Override
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public Asset getAsset(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));
    }

    @Override
    public Asset updateAssetStatus(UUID id, StatusRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));

        asset.setStatus(request.status());
        return assetRepository.save(asset);
    }

    // @Override
    // public List<Asset> getAssetsByStatus(Status status) {
    // // return assetRepository.findByStatus(status);
    // // }

    @Override
    public Asset updateAssetLastSeen(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + id));

        asset.setLastSeen(Instant.now());
        return assetRepository.save(asset);
    }

    @Override
    @Transactional
    public void updateRisk(UUID assetId, double newRiskScore) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("No Asset found with id " + assetId));

        int rounded = (int) Math.round(newRiskScore);
        asset.setRiskScore(rounded);
        asset.setRiskLevel(RiskLevel.fromScore(rounded)); // ← auto-update level
        assetRepository.save(asset);

        // update cache too
        cacheService.updateRiskInCache(asset.getMacAddress(), rounded,
                asset.getRiskLevel().name());
    }

    @Override
    public void deleteAsset(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Asset ID cannot be null");
        }
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("No Asset found with id " + id);
        }
        assetRepository.deleteById(id);
    }

    @Transactional
    public Asset upsertFromEvent(String mac, String ip, Integer port, String protocol, String deviceType) {

        if ((mac == null || mac.isBlank()) && (ip == null || ip.isBlank())) {
            throw new IllegalArgumentException("Either MAC or IP must be provided");
        }

        Optional<Asset> optional = Optional.empty();

        // 🔥 Prefer MAC (true identity)
        if (mac != null && !mac.isBlank()) {
            optional = assetRepository.findByMacAddress(mac);
        }

        // ⚠️ fallback to IP ONLY if MAC not available
        if (optional.isEmpty() && ip != null && !ip.isBlank()) {
            optional = assetRepository.findByIpAddress(ip);
        }

        Asset asset;

        if (optional.isPresent()) {
            asset = optional.get();

            // 🔄 update identity fields safely
            if (mac != null && asset.getMacAddress() == null) {
                asset.setMacAddress(mac);
            }

            if (ip != null) {
                asset.setIpAddress(ip);
            }

            asset.setLastSeen(Instant.now());
            asset.setCommunicationCount(asset.getCommunicationCount() + 1);

            // CIS-1: set deviceType only if not already classified
            if (deviceType != null && !deviceType.isBlank() && asset.getDeviceType() == null) {
                try {
                    asset.setDeviceType(com.sr.asset_service.Enums.DeviceType.valueOf(deviceType));
                } catch (IllegalArgumentException ignored) {
                    // unknown classification string — leave as null
                }
            }

            if (port != null) {
                asset.getPortsUsed().add(port);
            }

            if (protocol != null && !protocol.isBlank()) {
                asset.getProtocolsSeen().add(protocol);
            }

        } else {
            // CIS-1: auto-create asset with classification from RISK-ENGINE discovery
            com.sr.asset_service.Enums.DeviceType resolvedType = null;
            if (deviceType != null && !deviceType.isBlank()) {
                try {
                    resolvedType = com.sr.asset_service.Enums.DeviceType.valueOf(deviceType);
                } catch (IllegalArgumentException ignored) { /* unknown string — leave null */ }
            }
            asset = Asset.builder()
                    .macAddress(mac)
                    .ipAddress(ip)
                    .deviceType(resolvedType)  // CIS-1: typed device inventory
                    .firstSeen(Instant.now())
                    .lastSeen(Instant.now())
                    .communicationCount(1)
                    .portsUsed(port != null ? new HashSet<>(Set.of(port)) : new HashSet<>())
                    .protocolsSeen(
                            protocol != null && !protocol.isBlank()
                                    ? new HashSet<>(Set.of(protocol))
                                    : new HashSet<>())
                    .riskScore(0)
                    .riskLevel(RiskLevel.LOW)
                    .status(Status.ACTIVE)
                    .build();
        }

        Asset saved;

        try {
            saved = assetRepository.save(asset);
        } catch (DataIntegrityViolationException e) {
            // 🧠 Race condition fallback (very important)
            // Another thread inserted same MAC at same time
            if (mac != null) {
                saved = assetRepository.findByMacAddress(mac)
                        .orElseThrow(() -> e); // rethrow if still not found
            } else {
                throw e;
            }
        }

        // ✅ Cache ONLY after successful commit
        Asset finalSaved = saved;
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        cacheService.cacheAsset(finalSaved);
                    }
                });

        return saved;
    }

}
