package com.sr.asset_service.repository;

import com.sr.asset_service.model.Asset;
import com.sr.asset_service.model.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByStatus(Status status);

    Optional<Asset> findByIpAddress(String ipAddress);

    Optional<Asset> findByMacAddress(String mac);
}
