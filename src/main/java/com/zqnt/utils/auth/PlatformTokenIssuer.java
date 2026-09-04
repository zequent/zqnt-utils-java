package com.zqnt.utils.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Mints a platform auth token — the issuing counterpart to {@link PlatformTokenVerifier}, same
 * compact-JWS/EdDSA shape, deliberately kept as the one and only signing mechanism this platform
 * uses for its own tokens (as opposed to the license server's leases, which this platform only ever
 * verifies, never issues).
 *
 * <p>Holding a {@code PlatformTokenIssuer} means holding the platform's private signing key —
 * unlike {@link PlatformTokenVerifier} (safe for every service to hold, it only needs the public
 * key), this belongs only to whichever service is actually the platform-hosted identity provider.
 * Today that's admin-console (the one HTTP-facing service); a federated customer IdP (see the
 * tenancy blueprint's Part B) mints its own tokens and never touches this class at all.</p>
 */
public class PlatformTokenIssuer {

    private final ObjectMapper objectMapper;
    private final String privateKeyPem;
    private final String issuer;

    public PlatformTokenIssuer(ObjectMapper objectMapper, String privateKeyPem, String issuer) {
        this.objectMapper = objectMapper;
        this.privateKeyPem = privateKeyPem;
        this.issuer = issuer;
    }

    public String issue(String subject, String organizationId, java.util.Set<String> roles, Duration validFor)
            throws PlatformTokenVerificationException {
        try {
            if (privateKeyPem == null || privateKeyPem.isBlank()) {
                throw new PlatformTokenVerificationException("auth.private-key is not configured");
            }
            Instant now = Instant.now();
            Instant expiresAt = now.plus(validFor);

            ObjectNode header = objectMapper.createObjectNode();
            header.put("alg", "EdDSA");
            header.put("typ", "JWT");

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("iss", issuer);
            payload.put("sub", subject);
            if (organizationId != null && !organizationId.isBlank()) {
                payload.put("org_id", organizationId);
            }
            var rolesArray = payload.putArray("roles");
            roles.forEach(rolesArray::add);
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", expiresAt.getEpochSecond());
            // Unique per issued token — lets a caller revoke this exact token (logout) without
            // affecting any other token the same user still holds. See PlatformClaims#jti.
            payload.put("jti", UUID.randomUUID().toString());

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String headerPart = encoder.encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadPart = encoder.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = headerPart + "." + payloadPart;

            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(readPrivateKey());
            signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            String signaturePart = encoder.encodeToString(signer.sign());

            return signingInput + "." + signaturePart;
        } catch (PlatformTokenVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformTokenVerificationException("Could not issue auth token", e);
        }
    }

    private PrivateKey readPrivateKey() throws Exception {
        String normalized = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}
