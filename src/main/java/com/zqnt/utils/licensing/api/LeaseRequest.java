package com.zqnt.utils.licensing.api;

public record LeaseRequest(
        String installationId,
        String product,
        String version) {
}
