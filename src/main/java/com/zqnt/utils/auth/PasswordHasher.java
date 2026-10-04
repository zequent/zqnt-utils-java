package com.zqnt.utils.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Hashes and verifies user passwords — PBKDF2WithHmacSHA256, JDK-native (no external dependency,
 * same "no new library for something java.security already does" choice as {@link
 * PlatformTokenVerifier}/{@link PlatformTokenIssuer}'s hand-rolled EdDSA JWS). Each call to
 * {@link #hash(char[])} generates a fresh random salt — two identical passwords never produce the
 * same stored value, so a leaked user table doesn't reveal which accounts share a password.
 *
 * <p>Encoded format: {@code pbkdf2$<iterations>$<base64 salt>$<base64 hash>} — the iteration count
 * travels with the hash so it can be raised later without invalidating passwords hashed under a
 * lower count; {@link #matches} reads whatever count the stored value says, not a hardcoded one.</p>
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 210_000; // OWASP 2023 minimum recommendation for PBKDF2-SHA256
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String PREFIX = "pbkdf2";

    private final SecureRandom random = new SecureRandom();

    public String hash(char[] password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, DEFAULT_ITERATIONS);
        return PREFIX + "$" + DEFAULT_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Constant-time comparison against the stored encoded hash — never short-circuits on the
     * first differing byte, so a timing attack can't be used to guess a password one byte at a
     * time. Returns false (never throws) for a malformed stored value — that's a data problem, not
     * grounds to leak information about why verification failed. */
    public boolean matches(char[] password, String encoded) {
        try {
            String[] parts = encoded.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
