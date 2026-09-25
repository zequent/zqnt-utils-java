package com.zqnt.utils.crypto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Verifies an {@link ExportSigner} signature — see that class for why this uses a dedicated
 * export-signing keypair rather than {@code com.zqnt.utils.auth.PlatformTokenVerifier}'s
 * JWT-verification key. Only admin-console ever needs this (import always happens through
 * admin-console, which is also the only service that ever mints a `.zqnt` file), so unlike {@code
 * ConfiguredPlatformTokenVerifier} this has no shared-component CDI wiring — its {@code Configured}
 * counterpart lives directly in admin-console.
 */
public class ExportVerifier {

    private static final String SIGNATURE_ALGORITHM = "Ed25519";

    private final String publicKeyPem;

    public ExportVerifier(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    /** @return true iff {@code signature} is a valid signature over {@code data} under the
     * configured public key. Never throws for "signature didn't match" — only for missing/malformed
     * key configuration or a malformed signature blob, which are caller/deployment bugs, not
     * ordinary verification failures. */
    public boolean verify(byte[] data, byte[] signature) throws ExportEnvelopeException {
        try {
            if (publicKeyPem == null || publicKeyPem.isBlank()) {
                throw new ExportEnvelopeException("export.signing-public-key is not configured");
            }
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(readPublicKey());
            verifier.update(data);
            return verifier.verify(signature);
        } catch (ExportEnvelopeException e) {
            throw e;
        } catch (SignatureException e) {
            // The JDK's EdDSA provider throws this — rather than just returning false from
            // verify() — for a signature blob that isn't even a well-formed point on the curve,
            // which a single flipped bit can easily produce. That's still just "this signature
            // doesn't verify" from a caller's point of view (a tampered/corrupted signature is
            // exactly this method's job to reject), not a key-configuration or deployment bug —
            // matches this method's own documented contract, see the doc comment above.
            return false;
        } catch (Exception e) {
            throw new ExportEnvelopeException("Could not verify export signature", e);
        }
    }

    private PublicKey readPublicKey() throws Exception {
        String normalized = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance(SIGNATURE_ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
    }
}
