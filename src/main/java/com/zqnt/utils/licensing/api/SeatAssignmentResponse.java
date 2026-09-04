package com.zqnt.utils.licensing.api;

import java.time.Instant;

public record SeatAssignmentResponse(Seat seat) {
    public record Seat(
            String id,
            String licenseId,
            String userId,
            String email,
            String status,
            Instant assignedAt,
            Instant releasedAt) {
    }
}
