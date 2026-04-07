package com.sr.ALERT_SERVICE.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.ALERT_SERVICE.db.RiskEvent;

public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {
    List<RiskEvent> findTop50ByOrderByTimestampDesc();
}
