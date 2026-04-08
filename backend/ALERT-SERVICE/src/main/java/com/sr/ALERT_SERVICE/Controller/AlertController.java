package com.sr.ALERT_SERVICE.Controller;

import org.springframework.web.bind.annotation.*;

import com.sr.ALERT_SERVICE.Repository.RiskEventRepository;
import com.sr.ALERT_SERVICE.Service.AlertConsumerService;
import com.sr.ALERT_SERVICE.db.RiskEvent;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final RiskEventRepository repository;

    @GetMapping("/history")
    public List<RiskEvent> getAlertHistory() {
        // Returns the last 50 alerts from our Docker Postgres

        return repository.findTop50ByOrderByTimestampDesc();
    }
}