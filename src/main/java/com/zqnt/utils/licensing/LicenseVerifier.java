package com.zqnt.utils.licensing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LicenseVerifier {

    private final ObjectMapper objectMapper;
    private final String publicKey;
    private final String expectedIssuer;
    private final String expectedAudience;
    private final String expectedInstallationId;

    public LicenseVerifier(
            ObjectMapper objectMapper,
            String publicKey,
            String expectedIssuer,
            String expectedAudience,
            String expectedInstallationId) {
        this.objectMapper = objectMapper;
        this.publicKey = publicKey;
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
        this.expectedInstallationId = expectedInstallationId;
    }

    public LicenseClaims verify(String compactJws) throws LicenseVerificationException {
        try {
            if (compactJws == null || compactJws.isBlank()) {
                throw new LicenseVerificationException("License lease is empty");
            }
            if (publicKey.isBlank()) {
                throw new LicenseVerificationException("licensing.public-key is not configured");
            }

            String[] parts = compactJws.split("\\.", -1);
            if (parts.length != 3) {
                throw new LicenseVerificationException("License lease must be a compact JWS");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            JsonNode header = objectMapper.readTree(decoder.decode(parts[0]));
            if (!"EdDSA".equals(header.path("alg").asText())) {
                throw new LicenseVerificationException("Only EdDSA license signatures are accepted");
            }

            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(readPublicKey());
            verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            if (!verifier.verify(decoder.decode(parts[2]))) {
                throw new LicenseVerificationException("License signature is invalid");
            }

            JsonNode payload = objectMapper.readTree(decoder.decode(parts[1]));
            LicenseClaims claims = readClaims(payload);
            validateIdentity(claims);
            return claims;
        } catch (LicenseVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new LicenseVerificationException("Could not verify license lease", e);
        }
    }

    private PublicKey readPublicKey() throws Exception {
        String normalized = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(encoded));
    }

    private LicenseClaims readClaims(JsonNode payload) throws LicenseVerificationException {
        Set<String> audience = readStringSet(payload.get("aud"));
        Set<String> features = readStringSet(payload.get("features"));
        Map<String, Long> limits = new HashMap<>();
        JsonNode limitsNode = payload.get("limits");
        if (limitsNode != null && limitsNode.isObject()) {
            limitsNode.fields().forEachRemaining(entry -> limits.put(entry.getKey(), entry.getValue().asLong()));
        }

        Instant expiresAt = epochSecond(payload, "exp", true);
        Instant graceUntil = epochSecond(payload, "grace_until", false);
        if (graceUntil == null) {
            graceUntil = expiresAt;
        }
        if (graceUntil.isBefore(expiresAt)) {
            throw new LicenseVerificationException("grace_until must not be before exp");
        }

        return new LicenseClaims(
                text(payload, "iss"),
                audience,
                text(payload, "jti"),
                text(payload, "license_id"),
                text(payload, "activation_id"),
                text(payload, "installation_id"),
                text(payload, "product"),
                features,
                limits,
                epochSecond(payload, "iat", false),
                epochSecond(payload, "nbf", false),
                expiresAt,
                graceUntil);
    }

    private void validateIdentity(LicenseClaims claims) throws LicenseVerificationException {
        if (expectedInstallationId.isBlank()) {
            throw new LicenseVerificationException("licensing.installation-id is not configured");
        }
        if (!expectedIssuer.equals(claims.issuer())) {
            throw new LicenseVerificationException("Unexpected license issuer");
        }
        if (!claims.audience().contains(expectedAudience)) {
            throw new LicenseVerificationException("License audience does not include this product");
        }
        if (!expectedAudience.equals(claims.product())) {
            throw new LicenseVerificationException("License was issued for another product");
        }
        if (!expectedInstallationId.equals(claims.installationId())) {
            throw new LicenseVerificationException("License belongs to another installation");
        }
    }

    private static String text(JsonNode payload, String field) throws LicenseVerificationException {
        String value = payload.path(field).asText("");
        if (value.isBlank()) {
            throw new LicenseVerificationException("Missing license claim: " + field);
        }
        return value;
    }

    private static Instant epochSecond(JsonNode payload, String field, boolean required)
            throws LicenseVerificationException {
        JsonNode node = payload.get(field);
        if (node == null || !node.canConvertToLong()) {
            if (required) {
                throw new LicenseVerificationException("Missing license claim: " + field);
            }
            return null;
        }
        return Instant.ofEpochSecond(node.asLong());
    }

    private static Set<String> readStringSet(JsonNode node) {
        Set<String> result = new HashSet<>();
        if (node == null) {
            return result;
        }
        if (node.isTextual()) {
            result.add(node.asText());
        } else if (node.isArray()) {
            node.forEach(value -> result.add(value.asText()));
        }
        return result;
    }
}
