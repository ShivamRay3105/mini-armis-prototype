package com.sr.Event_Ingestion_Service.dto;

import java.time.Instant;

import com.sr.Event_Ingestion_Service.model.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(

                @NotNull Long assetId,
                @NotBlank EventType eventType,
                @NotNull Instant timestamp,
                Integer port,        // CIS-13: optional — port number from the network event
                String macAddress    // CIS-1: optional — MAC address for asset identity

) {
}
