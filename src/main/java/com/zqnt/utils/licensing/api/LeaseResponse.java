package com.zqnt.utils.licensing.api;

import java.time.Instant;

public record LeaseResponse(
        String lease,
        long refreshAfterSeconds,
        Instant serverTime) {
}
