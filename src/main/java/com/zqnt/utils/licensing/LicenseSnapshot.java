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
        Instant lastRefreshAt,
        // Whose license this is, and the organization name the license was issued for (see
        // LicenseClaims#organizationName). Null when no lease is loaded or the hub did not say.
        String organizationId,
        String organizationName) {

    /** A snapshot without the organization fields. */
    public LicenseSnapshot(LicenseStatus status, String reason, String licenseId, String activationId,
            String installationId, String product, Set<String> features, Map<String, Long> limits,
            Instant expiresAt, Instant graceUntil, Instant lastRefreshAt) {
        this(status, reason, licenseId, activationId, installationId, product, features, limits,
                expiresAt, graceUntil, lastRefreshAt, null, null);
    }

    public boolean operational() {
        return status == LicenseStatus.VALID || status == LicenseStatus.GRACE;
    }
}
