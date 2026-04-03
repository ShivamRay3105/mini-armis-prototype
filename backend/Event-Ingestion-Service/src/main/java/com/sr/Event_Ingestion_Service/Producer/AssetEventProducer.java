package com.sr.Event_Ingestion_Service.Producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.sr.Event_Ingestion_Service.model.AssetEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AssetEventProducer {

    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;
    private static final String TOPIC = "asset-events";

    @PostMapping("/sendEvent")
    public void sendEvent(@RequestBody AssetEvent event) {
        log.info("Pushing Asset Event to Kafka for Asset: {}", event.assetId());

        kafkaTemplate.send(TOPIC, event.assetId().toString(), event); // Drops letter in mailbox
    }

}
