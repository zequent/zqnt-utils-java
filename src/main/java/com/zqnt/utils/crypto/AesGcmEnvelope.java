package com.zqnt.utils.crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AES-256-GCM envelope encryption, JDK-native (no external crypto library — same "no new
 * dependency for something java.security already does" choice as {@code
 * com.zqnt.utils.auth.PlatformTokenIssuer}/{@code PasswordHasher}'s hand-rolled EdDSA/PBKDF2). Used
 * by the {@code .zqnt} export/import format: a random per-file data-encryption key (DEK) encrypts
 * the payload, and the DEK itself is wrapped under one of two key-encrypting keys (KEK) —
 * {@link #deriveOrgKek} for the no-passphrase platform-key default, or
 * {@link #deriveKeyFromPassphrase} for out-of-band transfer between installations.
 *
 * <p>{@link #encrypt}/{@link #decrypt} are also what {@link #wrapKey}/{@link #unwrapKey} use
 * internally to wrap the DEK's raw bytes under the KEK — one primitive, two roles, rather than a
 * separate key-wrap algorithm.</p>
 */
public final class AesGcmEnvelope {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_NONCE_LENGTH_BYTES = 12;
    private static final int AES_KEY_LENGTH_BYTES = 32; // 256-bit

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_KEY_LENGTH_BITS = AES_KEY_LENGTH_BYTES * 8;
    /** Matches {@code PasswordHasher}'s OWASP-2023 baseline for PBKDF2-SHA256. A `.zqnt` file is
     * decrypted rarely (an explicit import action, not an interactive login), so running this hot
     * costs nothing real in practice. */
    public static final int DEFAULT_PASSPHRASE_ITERATIONS = 210_000;
    public static final int SALT_LENGTH_BYTES = 16;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int HMAC_OUTPUT_LENGTH_BYTES = 32;
    private static final byte[] ORG_KEK_INFO_PREFIX = "zqnt-export-kek:".getBytes(StandardCharsets.UTF_8);

    private static final SecureRandom RANDOM = new SecureRandom();

    private AesGcmEnvelope() {
    }

    /** A fresh random 256-bit AES key — the per-export data-encryption key (DEK). Never reused
     * across exports, regardless of which KEK mode wraps it. */
    public static SecretKey generateDek() {
        byte[] raw = new byte[AES_KEY_LENGTH_BYTES];
        RANDOM.nextBytes(raw);
        return new SecretKeySpec(raw, AES_ALGORITHM);
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /** {@code nonce(12) || ciphertext+tag} — the standard GCM wire convention. The 128-bit auth tag
     * is GCM's own tamper detection; no separate HMAC is layered on top. */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) throws ExportEnvelopeException {
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTES];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] envelope = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, envelope, 0, nonce.length);
            System.arraycopy(ciphertext, 0, envelope, nonce.length, ciphertext.length);
            return envelope;
        } catch (GeneralSecurityException e) {
            throw new ExportEnvelopeException("Could not encrypt export payload", e);
        }
    }

    /** Throws {@link ExportEnvelopeException} — deliberately not distinguishing "wrong key" from
     * "corrupted/tampered ciphertext" (both surface as {@link AEADBadTagException}; GCM's auth tag
     * can't tell them apart, so this class doesn't pretend to either). */
    public static byte[] decrypt(byte[] envelope, SecretKey key) throws ExportEnvelopeException {
        if (envelope == null || envelope.length <= GCM_NONCE_LENGTH_BYTES) {
            throw new ExportEnvelopeException("Encrypted payload is too short to be valid");
        }
        try {
            byte[] nonce = Arrays.copyOfRange(envelope, 0, GCM_NONCE_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(envelope, GCM_NONCE_LENGTH_BYTES, envelope.length);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            return cipher.doFinal(ciphertext);
        } catch (AEADBadTagException e) {
            throw new ExportEnvelopeException("Payload failed to decrypt — wrong key/passphrase, or the file was tampered with", e);
        } catch (GeneralSecurityException e) {
            throw new ExportEnvelopeException("Could not decrypt export payload", e);
        }
    }

    public static byte[] wrapKey(SecretKey keyToWrap, SecretKey kek) throws ExportEnvelopeException {
        return encrypt(keyToWrap.getEncoded(), kek);
    }

    public static SecretKey unwrapKey(byte[] wrapped, SecretKey kek) throws ExportEnvelopeException {
        return new SecretKeySpec(decrypt(wrapped, kek), AES_ALGORITHM);
    }

    /** PBKDF2WithHmacSHA256 over a user-supplied passphrase — the out-of-band-transfer KEK mode.
     * Same salt + iteration count must be supplied on both encrypt and decrypt (carried in the
     * `.zqnt` manifest's {@code encryption} block, in plaintext — a salt/iteration-count isn't
     * secret, only the passphrase is). */
    public static SecretKey deriveKeyFromPassphrase(char[] passphrase, byte[] salt, int iterations)
            throws ExportEnvelopeException {
        try {
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, iterations, PBKDF2_KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] raw = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(raw, AES_ALGORITHM);
        } catch (GeneralSecurityException e) {
            throw new ExportEnvelopeException("Could not derive key from passphrase", e);
        }
    }

    /** HKDF-SHA256 (RFC 5869) over the installation's platform export key, with the exporting
     * organization's id as the {@code info} parameter — the no-passphrase KEK mode. Deriving a
     * distinct key per organization (rather than using {@code platformKek} directly as the KEK)
     * means two organizations sharing one installation's platform key still can't decrypt each
     * other's platform-mode exports, even though neither ever entered a passphrase. Only ever needs
     * a single expand block (32-byte output == SHA-256's output length), so this implements the
     * single-block case of RFC 5869 §2.3, not the general multi-block loop. */
    public static SecretKey deriveOrgKek(byte[] platformKek, String organizationId) throws ExportEnvelopeException {
        if (platformKek == null || platformKek.length == 0) {
            throw new ExportEnvelopeException("export.platform-kek is not configured");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new ExportEnvelopeException("Cannot derive a platform-mode export key without an organization id");
        }
        try {
            // Extract: PRK = HMAC-Hash(salt, IKM). A fixed, non-secret application-specific salt is
            // used (not the all-zero salt RFC 5869 falls back to when none is given) so this
            // derivation can't collide with any other HKDF use of the same platform key elsewhere.
            byte[] prk = hmacSha256("zqnt-export-kek-salt".getBytes(StandardCharsets.UTF_8), platformKek);

            // Expand, single block: T(1) = HMAC-Hash(PRK, info || 0x01).
            byte[] info = concat(ORG_KEK_INFO_PREFIX, organizationId.getBytes(StandardCharsets.UTF_8));
            byte[] expandInput = concat(info, new byte[]{0x01});
            byte[] okm = hmacSha256(prk, expandInput);

            return new SecretKeySpec(Arrays.copyOf(okm, AES_KEY_LENGTH_BYTES), AES_ALGORITHM);
        } catch (GeneralSecurityException e) {
            throw new ExportEnvelopeException("Could not derive per-organization export key", e);
        }
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        byte[] result = mac.doFinal(data);
        assert result.length == HMAC_OUTPUT_LENGTH_BYTES;
        return result;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
