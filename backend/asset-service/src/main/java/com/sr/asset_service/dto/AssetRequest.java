package com.sr.asset_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssetRequest(
        @NotBlank(message = "Give device type") String deviceType,

        @NotBlank String protocol,

        @NotBlank String zone,

        @NotBlank @Pattern(regexp = "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$", message = "Invalid ipAddress format") String ipAddress) {
    public AssetRequest {
        if (deviceType != null)
            deviceType = deviceType.trim().toUpperCase();
        if (protocol != null)
            protocol = protocol.trim().toUpperCase();
        if (zone != null)
            zone = zone.trim().toUpperCase();
    }
}
