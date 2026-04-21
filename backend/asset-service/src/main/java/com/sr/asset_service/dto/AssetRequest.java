package com.sr.asset_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssetRequest(

        @NotBlank(message = "MAC address is required") @Pattern(regexp = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$", message = "Invalid MAC address format (expected AA:BB:CC:DD:EE:FF)") String macAddress,

        @NotBlank(message = "Give device type") String deviceType,

        @NotBlank String protocol,

        @NotBlank String zone,

        @NotBlank @Pattern(regexp = "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$", message = "Invalid ipAddress format") String ipAddress,

        String hostname, // NEW — optional

        String vendor) {

    public AssetRequest {
        if (deviceType != null)
            deviceType = deviceType.trim().toUpperCase();
        if (protocol != null)
            protocol = protocol.trim().toUpperCase();
        if (zone != null)
            zone = zone.trim().toUpperCase();
        if (macAddress != null)
            macAddress = macAddress.trim().toUpperCase();
    }
}
