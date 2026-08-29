package com.zqnt.utils.licensing;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record LicenseSnapshot(
        LicenseStatus status,
        String reason,
        String licenseId,
        String activationId,
        String installationId,
        String product,
        Set<String> features,
        Map<String, Long> limits,
        Instant expiresAt,
        Instant graceUntil,
        Instant lastRefreshAt) {

    public boolean operational() {
        return status == LicenseStatus.VALID || status == LicenseStatus.GRACE;
    }
}
