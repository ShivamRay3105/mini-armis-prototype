package com.sr.RISK_ENGINE.Service;

import java.util.UUID;

public interface IdempotencyService {

    boolean isDuplicate(UUID eventId);

}
