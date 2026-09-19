package com.zqnt.utils.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ExportSignerVerifierTest {

    private static final byte[] DATA = "the manifest bytes this signature covers".getBytes(StandardCharsets.UTF_8);

    private String privateKeyPem;
    private String publicKeyPem;
    private ExportSigner signer;
    private ExportVerifier verifier;

    @BeforeEach
    void generateKeypair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = generator.generateKeyPair();
        privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + "\n"
                + "-----END PRIVATE KEY-----";
        publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + "\n"
                + "-----END PUBLIC KEY-----";
        signer = new ExportSigner(privateKeyPem);
        verifier = new ExportVerifier(publicKeyPem);
    }

    @Test
    void aValidSignatureVerifies() throws Exception {
        byte[] signature = signer.sign(DATA);
        assertTrue(verifier.verify(DATA, signature));
    }

    @Test
    void aTamperedByteInTheSignedRangeFailsVerification() throws Exception {
        byte[] signature = signer.sign(DATA);
        byte[] tampered = DATA.clone();
        tampered[0] ^= 0xFF;
        assertFalse(verifier.verify(tampered, signature));
    }

    @Test
    void aTamperedSignatureFailsVerification() throws Exception {
        byte[] signature = signer.sign(DATA);
        signature[0] ^= 0xFF;
        assertFalse(verifier.verify(DATA, signature));
    }

    @Test
    void verifyingWithADifferentKeypairFails() throws Exception {
        byte[] signature = signer.sign(DATA);
        KeyPair otherKeyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        String otherPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(otherKeyPair.getPublic().getEncoded()) + "\n"
                + "-----END PUBLIC KEY-----";
        ExportVerifier otherVerifier = new ExportVerifier(otherPublicKeyPem);
        assertFalse(otherVerifier.verify(DATA, signature));
    }

    @Test
    void signingWithoutAConfiguredPrivateKeyThrows() {
        ExportSigner unconfigured = new ExportSigner("");
        assertThrows(ExportEnvelopeException.class, () -> unconfigured.sign(DATA));
    }

    @Test
    void verifyingWithoutAConfiguredPublicKeyThrows() throws Exception {
        byte[] signature = signer.sign(DATA);
        ExportVerifier unconfigured = new ExportVerifier("");
        assertThrows(ExportEnvelopeException.class, () -> unconfigured.verify(DATA, signature));
    }
}
