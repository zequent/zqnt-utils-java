package com.zqnt.utils.licensing.api;

public record LicenseProblem(
        String type,
        String title,
        int status,
        String code,
        String detail,
        String requestId,
        boolean retryable) {
}
