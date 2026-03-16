package com.sr.Event_Ingestion_Service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.sr.Event_Ingestion_Service.model.AssetEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor

public class AssetEventProducer {

    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;
    private static final String TOPIC = "asset-events";

    @GetMapping("/sendEvent")
    public String sendEvent(AssetEvent event) {
        kafkaTemplate.send(TOPIC, event.assetId().toString(), event); // Drops letter in mailbox
        return "Sent!";
    }

}
