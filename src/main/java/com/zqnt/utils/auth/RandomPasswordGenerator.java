package com.zqnt.utils.auth;

import java.security.SecureRandom;

/** Generates a one-time, human-typeable initial password for a newly created account — used
 * wherever a user is created by an admin action rather than self-service signup (see
 * {@code SystemAdminUserSeeder}, admin-console's user-creation endpoint), so the same alphabet and
 * length policy isn't reinvented at each call site. The caller is always expected to show this
 * value to whoever's creating the account exactly once and never persist it in plaintext — only
 * {@link PasswordHasher#hash} ever gets stored. */
public final class RandomPasswordGenerator {

    private static final String ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";
    private static final int LENGTH = 24;

    private static final SecureRandom random = new SecureRandom();

    private RandomPasswordGenerator() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
