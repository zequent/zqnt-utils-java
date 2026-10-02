package com.zqnt.utils.auth;

/** Thrown by {@link PlatformTokenVerifier} for anything wrong with a bearer token — malformed,
 * badly signed, expired, or issued by someone other than who's configured as trusted. A caller
 * catching this should reject the call (UNAUTHENTICATED), never silently proceed as if no token had
 * been presented at all — "invalid token" and "no token" are different, and only the latter is safe
 * to fall back on during the transitional period before an issuer exists (see TenantContext). */
public class PlatformTokenVerificationException extends Exception {
    public PlatformTokenVerificationException(String message) {
        super(message);
    }

    public PlatformTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
