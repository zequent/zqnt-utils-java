package com.zqnt.utils.licensing.api;

import java.time.Instant;

public record ActivationResponse(
        String activationId,
        String activationToken,
        String lease,
        long refreshAfterSeconds,
        Instant serverTime) {
}
