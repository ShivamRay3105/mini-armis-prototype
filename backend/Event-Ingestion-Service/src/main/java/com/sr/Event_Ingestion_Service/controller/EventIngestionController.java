package com.sr.Event_Ingestion_Service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.sr.Event_Ingestion_Service.dto.EventRequest;
import com.sr.Event_Ingestion_Service.dto.EventResponse;
import com.sr.Event_Ingestion_Service.model.AssetEvent;
import com.sr.Event_Ingestion_Service.service.EventIngestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;

    @PostMapping("/stimulate")
    public ResponseEntity<EventResponse> stimulateEvent(@Valid @RequestBody EventRequest eventRequest) {

        AssetEvent event = eventIngestionService.ingestEvent(eventRequest);

        EventResponse response = new EventResponse(event.eventId(), "ACCEPTED");

        return ResponseEntity.ok(response);

    }

}
