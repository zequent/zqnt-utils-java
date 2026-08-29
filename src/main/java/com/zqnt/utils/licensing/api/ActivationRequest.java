package com.zqnt.utils.licensing.api;

public record ActivationRequest(
        String licenseKey,
        String installationId,
        String product,
        String version) {
}
