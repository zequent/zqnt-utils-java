package com.zqnt.utils.licensing;

public class LicenseVerificationException extends Exception {
    public LicenseVerificationException(String message) {
        super(message);
    }

    public LicenseVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
