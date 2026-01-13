package com.sr.asset_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAssetRequest(
    @NotBlank(message = "Device type bhenchod dena padega") 
    String deviceType,

    @NotBlank 
    String protocol,

    @NotBlank 
    String zone,

    @NotBlank 
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "IP galat hai chutiye") 
    String ipAddress
) {
    public CreateAssetRequest {
        if (deviceType != null) {
            deviceType = deviceType.trim().toUpperCase();
        }
    }
}
