package com.zqnt.utils.crypto;

/** Thrown by anything in this package — a bad signature, a wrong passphrase, a corrupt/tampered
 * envelope, or missing key configuration. Checked, same reasoning as {@code
 * com.zqnt.utils.auth.PlatformTokenVerificationException}: a caller must handle "this file/key is
 * bad" explicitly rather than let it escape as an unchecked bug. Deliberately does not distinguish
 * "wrong passphrase" from "tampered ciphertext" in its own type — AES-GCM's auth tag can't tell
 * those apart either (both just fail to authenticate), so callers shouldn't be given false
 * precision; the message is kept generic for the same reason {@code AuthenticationService} never
 * says which of email/password was wrong. */
public class ExportEnvelopeException extends Exception {
    public ExportEnvelopeException(String message) {
        super(message);
    }

    public ExportEnvelopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
