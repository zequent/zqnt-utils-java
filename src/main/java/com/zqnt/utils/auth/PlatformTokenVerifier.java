package com.zqnt.utils.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Verifies a platform auth token — a compact JWS, same shape and same verification approach as
 * {@link com.zqnt.utils.licensing.LicenseVerifier} (EdDSA over a configured public key, no external
 * JWT library dependency), deliberately kept consistent with it rather than reaching for a second,
 * differently-shaped mechanism for what is structurally the same problem.
 *
 * <p>Whichever identity provider actually issues these (platform-hosted default, or a federated
 * customer IdP via the broker — see the tenancy blueprint's Part B) is expected to mint a token in
 * this exact claim shape; this class doesn't care which one it was.</p>
 */
public class PlatformTokenVerifier {

    private final ObjectMapper objectMapper;
    private final String publicKey;
    private final String expectedIssuer;

    /** @param expectedIssuer if blank, the issuer claim is read but not checked against anything —
     * deliberately permissive until Part B settles on a real issuer (platform-hosted IdP vs. a
     * customer's own broker realm); configure it once that's decided. */
    public PlatformTokenVerifier(ObjectMapper objectMapper, String publicKey, String expectedIssuer) {
        this.objectMapper = objectMapper;
        this.publicKey = publicKey;
        this.expectedIssuer = expectedIssuer;
    }

    public PlatformClaims verify(String compactJws) throws PlatformTokenVerificationException {
        try {
            if (compactJws == null || compactJws.isBlank()) {
                throw new PlatformTokenVerificationException("Auth token is empty");
            }
            if (publicKey == null || publicKey.isBlank()) {
                throw new PlatformTokenVerificationException("auth.public-key is not configured");
            }

            String[] parts = compactJws.split("\\.", -1);
            if (parts.length != 3) {
                throw new PlatformTokenVerificationException("Auth token must be a compact JWS");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            JsonNode header = objectMapper.readTree(decoder.decode(parts[0]));
            if (!"EdDSA".equals(header.path("alg").asText())) {
                throw new PlatformTokenVerificationException("Only EdDSA auth token signatures are accepted");
            }

            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(readPublicKey());
            verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            if (!verifier.verify(decoder.decode(parts[2]))) {
                throw new PlatformTokenVerificationException("Auth token signature is invalid");
            }

            JsonNode payload = objectMapper.readTree(decoder.decode(parts[1]));
            PlatformClaims claims = readClaims(payload);
            validateIdentity(claims);
            return claims;
        } catch (PlatformTokenVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformTokenVerificationException("Could not verify auth token", e);
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

    private PlatformClaims readClaims(JsonNode payload) throws PlatformTokenVerificationException {
        String subject = text(payload, "sub");
        String organizationId = payload.hasNonNull("org_id") ? payload.get("org_id").asText() : null;
        Set<String> roles = readStringSet(payload.get("roles"));

        Instant expiresAt = epochSecond(payload, "exp", true);
        Instant issuedAt = epochSecond(payload, "iat", false);
        if (expiresAt.isBefore(Instant.now())) {
            throw new PlatformTokenVerificationException("Auth token is expired");
        }

        return new PlatformClaims(
                payload.path("iss").asText(null),
                subject,
                organizationId,
                roles,
                issuedAt,
                expiresAt);
    }

    private void validateIdentity(PlatformClaims claims) throws PlatformTokenVerificationException {
        if (expectedIssuer != null && !expectedIssuer.isBlank() && !expectedIssuer.equals(claims.issuer())) {
            throw new PlatformTokenVerificationException("Unexpected auth token issuer");
        }
    }

    private static String text(JsonNode payload, String field) throws PlatformTokenVerificationException {
        String value = payload.path(field).asText("");
        if (value.isBlank()) {
            throw new PlatformTokenVerificationException("Missing auth token claim: " + field);
        }
        return value;
    }

    private static Instant epochSecond(JsonNode payload, String field, boolean required)
            throws PlatformTokenVerificationException {
        JsonNode node = payload.get(field);
        if (node == null || !node.canConvertToLong()) {
            if (required) {
                throw new PlatformTokenVerificationException("Missing auth token claim: " + field);
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
