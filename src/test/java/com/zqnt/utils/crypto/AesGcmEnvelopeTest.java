package com.zqnt.utils.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmEnvelopeTest {

    private static final byte[] PLAINTEXT = "the quick brown fox jumps over the lazy dog"
            .getBytes(StandardCharsets.UTF_8);

    @Test
    void encryptThenDecryptRoundTripsWithARandomDek() throws Exception {
        SecretKey dek = AesGcmEnvelope.generateDek();
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, dek);
        assertArrayEquals(PLAINTEXT, AesGcmEnvelope.decrypt(envelope, dek));
    }

    @Test
    void encryptThenDecryptRoundTripsWithAPassphraseDerivedKey() throws Exception {
        byte[] salt = AesGcmEnvelope.generateSalt();
        SecretKey key = AesGcmEnvelope.deriveKeyFromPassphrase("correct horse battery staple".toCharArray(),
                salt, AesGcmEnvelope.DEFAULT_PASSPHRASE_ITERATIONS);
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, key);
        assertArrayEquals(PLAINTEXT, AesGcmEnvelope.decrypt(envelope, key));
    }

    @Test
    void twoEncryptionsOfTheSamePlaintextProduceDifferentCiphertext() throws Exception {
        SecretKey dek = AesGcmEnvelope.generateDek();
        byte[] first = AesGcmEnvelope.encrypt(PLAINTEXT, dek);
        byte[] second = AesGcmEnvelope.encrypt(PLAINTEXT, dek);
        assertFalse(java.util.Arrays.equals(first, second), "random nonce should make repeated encryptions differ");
    }

    @Test
    void tamperingWithTheCiphertextIsDetected() throws Exception {
        SecretKey dek = AesGcmEnvelope.generateDek();
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, dek);
        envelope[envelope.length - 1] ^= 0xFF; // flip a byte inside the ciphertext/tag, not the nonce
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.decrypt(envelope, dek));
    }

    @Test
    void decryptingWithTheWrongKeyFails() throws Exception {
        SecretKey dek = AesGcmEnvelope.generateDek();
        SecretKey wrongKey = AesGcmEnvelope.generateDek();
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, dek);
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.decrypt(envelope, wrongKey));
    }

    @Test
    void decryptingWithTheWrongPassphraseFails() throws Exception {
        byte[] salt = AesGcmEnvelope.generateSalt();
        SecretKey right = AesGcmEnvelope.deriveKeyFromPassphrase("right passphrase".toCharArray(), salt,
                AesGcmEnvelope.DEFAULT_PASSPHRASE_ITERATIONS);
        SecretKey wrong = AesGcmEnvelope.deriveKeyFromPassphrase("wrong passphrase".toCharArray(), salt,
                AesGcmEnvelope.DEFAULT_PASSPHRASE_ITERATIONS);
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, right);
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.decrypt(envelope, wrong));
    }

    @Test
    void sameSaltAndIterationsDeriveTheSameKeyDeterministically() throws Exception {
        byte[] salt = AesGcmEnvelope.generateSalt();
        SecretKey a = AesGcmEnvelope.deriveKeyFromPassphrase("same passphrase".toCharArray(), salt,
                AesGcmEnvelope.DEFAULT_PASSPHRASE_ITERATIONS);
        SecretKey b = AesGcmEnvelope.deriveKeyFromPassphrase("same passphrase".toCharArray(), salt,
                AesGcmEnvelope.DEFAULT_PASSPHRASE_ITERATIONS);
        assertArrayEquals(a.getEncoded(), b.getEncoded());
    }

    @Test
    void wrapAndUnwrapKeyRoundTrips() throws Exception {
        SecretKey dek = AesGcmEnvelope.generateDek();
        SecretKey kek = AesGcmEnvelope.generateDek();
        byte[] wrapped = AesGcmEnvelope.wrapKey(dek, kek);
        SecretKey unwrapped = AesGcmEnvelope.unwrapKey(wrapped, kek);
        assertArrayEquals(dek.getEncoded(), unwrapped.getEncoded());
    }

    @Test
    void deriveOrgKekIsDeterministicForTheSameOrgAndPlatformKey() throws Exception {
        byte[] platformKek = "a-shared-installation-platform-key-32b!".getBytes(StandardCharsets.UTF_8);
        SecretKey a = AesGcmEnvelope.deriveOrgKek(platformKek, "org-a");
        SecretKey b = AesGcmEnvelope.deriveOrgKek(platformKek, "org-a");
        assertArrayEquals(a.getEncoded(), b.getEncoded());
    }

    @Test
    void deriveOrgKekDiffersBetweenOrganizationsUnderTheSamePlatformKey() throws Exception {
        byte[] platformKek = "a-shared-installation-platform-key-32b!".getBytes(StandardCharsets.UTF_8);
        SecretKey orgA = AesGcmEnvelope.deriveOrgKek(platformKek, "org-a");
        SecretKey orgB = AesGcmEnvelope.deriveOrgKek(platformKek, "org-b");
        assertFalse(java.util.Arrays.equals(orgA.getEncoded(), orgB.getEncoded()),
                "two orgs sharing one platform key must not derive the same KEK");
    }

    @Test
    void anExportEncryptedForOneOrgCannotBeDecryptedAsAnother() throws Exception {
        byte[] platformKek = "a-shared-installation-platform-key-32b!".getBytes(StandardCharsets.UTF_8);
        SecretKey orgAKek = AesGcmEnvelope.deriveOrgKek(platformKek, "org-a");
        SecretKey orgBKek = AesGcmEnvelope.deriveOrgKek(platformKek, "org-b");
        byte[] envelope = AesGcmEnvelope.encrypt(PLAINTEXT, orgAKek);
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.decrypt(envelope, orgBKek));
    }

    @Test
    void deriveOrgKekRejectsAMissingPlatformKey() {
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.deriveOrgKek(new byte[0], "org-a"));
    }

    @Test
    void deriveOrgKekRejectsAMissingOrganizationId() {
        byte[] platformKek = "a-shared-installation-platform-key-32b!".getBytes(StandardCharsets.UTF_8);
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.deriveOrgKek(platformKek, null));
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.deriveOrgKek(platformKek, " "));
    }

    @Test
    void decryptRejectsAnEnvelopeShorterThanTheNonce() {
        SecretKey dek = AesGcmEnvelope.generateDek();
        assertThrows(ExportEnvelopeException.class, () -> AesGcmEnvelope.decrypt(new byte[]{1, 2, 3}, dek));
    }
}
