package com.sr.Event_Ingestion_Service.Producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.sr.Event_Ingestion_Service.model.AssetEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * CIS Control 8 — Audit Log Management:
 * Every Kafka send now has a callback. Failed sends are logged with the eventId
 * so they can be correlated, replayed, or audited. This ensures the event
 * pipeline has observable, traceable delivery.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class AssetEventProducer {

    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;
    private static final String TOPIC = "asset-events";

    @PostMapping("/sendEvent")
    public void sendEvent(@RequestBody AssetEvent event) {

        log.info("Pushing Asset Event to Kafka for Asset: {} | EventId: {}", event.assetId(), event.eventId());

        try {
            var result = kafkaTemplate.send(TOPIC, event.assetId().toString(), event).get(2, java.util.concurrent.TimeUnit.SECONDS);
            log.info("[CIS-8] Kafka send SUCCESS: eventId={} → topic={} partition={} offset={}",
                    event.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception ex) {
            log.error("[CIS-8] Kafka send FAILED for eventId={} assetId={}: {}",
                    event.eventId(), event.assetId(), ex.getMessage());
            throw new org.springframework.kafka.KafkaException("Kafka delivery failed", ex);
        }
    }
}
