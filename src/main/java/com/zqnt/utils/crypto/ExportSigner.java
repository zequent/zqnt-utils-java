package com.zqnt.utils.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Signs the byte range of a {@code .zqnt} export envelope — same EdDSA mechanism as {@code
 * com.zqnt.utils.auth.PlatformTokenIssuer}, but over a raw byte blob instead of a compact-JWS
 * header/payload pair: an export file isn't a bearer token and doesn't need {@code alg}/{@code typ}
 * header framing, so this deliberately doesn't force-fit the JWS shape onto it.
 *
 * <p>Uses a keypair dedicated to export signing (config keys {@code export.signing-private-key}/
 * {@code export.signing-public-key}), independent of {@code auth.private-key} (the JWT-issuing
 * key) — a compromised signing key here shouldn't also let someone forge login tokens, and vice
 * versa; the two keys also have very different validity lifetimes (a `.zqnt` file may be
 * re-imported months later, a JWT lives an hour).</p>
 */
public class ExportSigner {

    private static final String SIGNATURE_ALGORITHM = "Ed25519";

    private final String privateKeyPem;

    public ExportSigner(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }

    public byte[] sign(byte[] data) throws ExportEnvelopeException {
        try {
            if (privateKeyPem == null || privateKeyPem.isBlank()) {
                throw new ExportEnvelopeException("export.signing-private-key is not configured");
            }
            Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
            signer.initSign(readPrivateKey());
            signer.update(data);
            return signer.sign();
        } catch (ExportEnvelopeException e) {
            throw e;
        } catch (Exception e) {
            throw new ExportEnvelopeException("Could not sign export", e);
        }
    }

    private PrivateKey readPrivateKey() throws Exception {
        String normalized = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance(SIGNATURE_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}
