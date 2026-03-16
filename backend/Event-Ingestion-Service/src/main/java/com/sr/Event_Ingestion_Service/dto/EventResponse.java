package com.sr.Event_Ingestion_Service.dto;

import java.util.UUID;

public record EventResponse(
                UUID eventId,
                String status) {
}
