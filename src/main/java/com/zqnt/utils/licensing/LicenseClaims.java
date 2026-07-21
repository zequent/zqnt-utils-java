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
}
