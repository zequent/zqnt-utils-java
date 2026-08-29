package com.zqnt.utils.licensing;

public enum LicenseFeature {
    LIVE_DATA_READ("live-data.read"),
    LIVE_DATA_INGEST("live-data.ingest"),
    LIVE_STREAM("live-stream"),
    CAMERA_CONTROL("camera-control"),
    REMOTE_CONTROL("remote-control"),
    MISSION_AUTONOMY("mission-autonomy");

    private final String code;

    LicenseFeature(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
