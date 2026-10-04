package com.zqnt.utils.licensing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseVerifierTest {

    private KeyPair keyPair;
    private LicenseVerifier verifier;

    @BeforeEach
    void setUp() throws Exception {
        keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        verifier = new LicenseVerifier(new ObjectMapper(), publicKey,
                "https://api.zequent.com", "zqnt-framework", "installation-1");
    }

    @Test
    void acceptsValidSignedLease() throws Exception {
        long now = Instant.now().getEpochSecond();
        String payload = """
                {"iss":"https://api.zequent.com","aud":"zqnt-framework","jti":"lease-1",
                 "license_id":"license-1","activation_id":"activation-1",
                 "installation_id":"installation-1","product":"zqnt-framework",
                 "features":["live-data.read","live-stream"],"limits":{"assets":25},
                 "iat":%d,"nbf":%d,"exp":%d,"grace_until":%d}
                """.formatted(now, now - 1, now + 3600, now + 7200);

        LicenseClaims claims = verifier.verify(sign(payload));

        assertEquals("license-1", claims.licenseId());
        assertEquals(25, claims.limits().get("assets"));
        assertTrue(claims.hasFeature(LicenseFeature.LIVE_STREAM));
    }

    @Test
    void readsTheSignedOrganizationNameAndDescription() throws Exception {
        long now = Instant.now().getEpochSecond();
        String payload = validPayload(now).replace("\"product\":\"zqnt-framework\",",
                "\"product\":\"zqnt-framework\",\"organization_id\":\"6c1f1c52-1d3e-4b8a-9d61-3a3f0c8f2a11\","
                        + "\"organization_name\":\" Acme Robotics \",\"organization_description\":\"Plant 4\",");

        LicenseClaims claims = verifier.verify(sign(payload));

        assertEquals("6c1f1c52-1d3e-4b8a-9d61-3a3f0c8f2a11", claims.organizationId());
        assertEquals("Acme Robotics", claims.organizationName());
        assertEquals("Plant 4", claims.organizationDescription());
    }

    @Test
    void aLeaseWithoutOrganizationNameClaimsStillVerifies() throws Exception {
        long now = Instant.now().getEpochSecond();

        LicenseClaims claims = verifier.verify(sign(validPayload(now)));

        assertNull(claims.organizationName());
        assertNull(claims.organizationDescription());
    }

    @Test
    void rejectsTamperedPayload() throws Exception {
        long now = Instant.now().getEpochSecond();
        String signed = sign(validPayload(now).replace("live-stream", "live-data.read"));
        String[] parts = signed.split("\\.");
        String changedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(validPayload(now).getBytes(StandardCharsets.UTF_8));

        assertThrows(LicenseVerificationException.class,
                () -> verifier.verify(parts[0] + "." + changedPayload + "." + parts[2]));
    }

    @Test
    void rejectsLeaseForDifferentInstallation() throws Exception {
        long now = Instant.now().getEpochSecond();
        String payload = validPayload(now).replace("installation-1", "installation-2");

        assertThrows(LicenseVerificationException.class, () -> verifier.verify(sign(payload)));
    }

    private String validPayload(long now) {
        return """
                {"iss":"https://api.zequent.com","aud":["zqnt-framework"],"jti":"lease-1",
                 "license_id":"license-1","activation_id":"activation-1",
                 "installation_id":"installation-1","product":"zqnt-framework",
                 "features":["live-stream"],"exp":%d,"grace_until":%d}
                """.formatted(now + 3600, now + 7200);
    }

    private String sign(String payload) throws Exception {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"EdDSA\",\"typ\":\"JWT\"}"
                .getBytes(StandardCharsets.UTF_8));
        String body = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + body;
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(keyPair.getPrivate());
        signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + "." + encoder.encodeToString(signature.sign());
    }
}
