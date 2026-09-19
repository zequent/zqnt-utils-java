package com.zqnt.utils.licensing;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record LicenseClaims(
        String issuer,
        Set<String> audience,
        String leaseId,
        String licenseId,
        String activationId,
        String installationId,
        // Which zqnt-platform Organization this license belongs to — see the tenancy blueprint's
        // Part C. Null only for a legacy lease minted before org-scoped licensing existed; every
        // lease issued by the current zqnt-hub always sets it (see LicenseVerifier's identity
        // check, which does NOT require this — a legacy lease from an older hub build should
        // still verify, just without per-org enforcement being possible for it).
        String organizationId,
        String product,
        Set<String> features,
        Map<String, Long> limits,
        Instant issuedAt,
        Instant notBefore,
        Instant expiresAt,
        Instant graceUntil) {

    public LicenseClaims {
        audience = audience == null ? Set.of() : Set.copyOf(audience);
        features = features == null ? Set.of() : Set.copyOf(features);
        limits = limits == null ? Map.of() : Map.copyOf(limits);
    }

    public boolean hasFeature(LicenseFeature feature) {
        return features.contains("*") || features.contains(feature.code());
    }

    /** How many of this org's license seats are actually assigned right now — 0 if the issuing
     * hub predates seat tracking (an older lease's limits simply won't have this key). */
    public long seatsUsed() {
        return limits.getOrDefault("seats_used", 0L);
    }

    /** {@code -1} (unbounded) for a legacy lease with no seat_limit claim — see {@link #seatsUsed()}. */
    public long seatLimit() {
        return limits.getOrDefault("seat_limit", -1L);
    }

    /** False only when the seat limit is both known and already reached — a legacy lease with no
     * seat data at all is permissive by default, same reasoning as {@link #seatLimit()}. */
    public boolean hasSeatAvailable() {
        long limit = seatLimit();
        return limit < 0 || seatsUsed() < limit;
    }
}
