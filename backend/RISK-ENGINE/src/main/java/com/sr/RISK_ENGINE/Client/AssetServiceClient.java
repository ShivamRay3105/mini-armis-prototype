package com.sr.RISK_ENGINE.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sr.RISK_ENGINE.dto.UpdateRiskRequest;

@FeignClient(name = "asset-service")
public interface AssetServiceClient {

    @PutMapping("/api/v1/assets/update-risk")
    ResponseEntity<Void> updateRiskScore(@RequestBody UpdateRiskRequest request);
}
