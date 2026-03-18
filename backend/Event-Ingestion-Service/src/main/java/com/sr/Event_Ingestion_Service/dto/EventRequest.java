package com.sr.Event_Ingestion_Service.dto;

import java.time.Instant;

import com.sr.Event_Ingestion_Service.model.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(

                @NotNull Long assetId,
                @NotBlank EventType eventType,
                @NotNull Instant timestamp

) {
}
