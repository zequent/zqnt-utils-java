package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zqnt.utils.mission.proto.TaskTypeProto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration template for detect/recognition actions.
 * Performs object detection, recognition, or analysis during flight or hover.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DetectTaskConfig implements TaskConfigTemplate {


    /**
     * Detection targets (REQUIRED)
     * e.g., ["person", "vehicle", "anomaly", "thermal_hotspot"]
     */
    @NonNull
    @Builder.Default
    private List<String> detectionTargets = new ArrayList<>();

    /**
     * Detection mode (continuous, single_pass, hover_scan, patrol)
     * - continuous: Detect while performing other flight operations
     * - single_pass: One detection pass over area
     * - hover_scan: Hover at position and scan
     * - patrol: Patrol pattern with detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "continuous")
    private String detectionMode = "continuous";

    /**
     * Detection area - latitude center
     */
    private Double areaLatitude;

    /**
     * Detection area - longitude center
     */
    private Double areaLongitude;

    /**
     * Detection area radius in meters
     */
    @Builder.Default
    @JsonProperty(defaultValue = "50.0")
    private Float areaRadius = 50.0f;

    /**
     * Detection altitude in meters
     */
    @Builder.Default
    @JsonProperty(defaultValue = "30.0")
    private Float detectionAltitude = 30.0f;

    /**
     * Scan pattern (grid, spiral, perimeter, random)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "grid")
    private String scanPattern = "grid";

    /**
     * Flight speed during detection in m/s
     */
    @Builder.Default
    @JsonProperty(defaultValue = "3.0")
    private Float scanSpeed = 3.0f;

    /**
     * Enable thermal/infrared detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean thermalDetection = false;

    /**
     * Enable RGB/visual detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean visualDetection = true;

    /**
     * Minimum detection confidence (0.0 - 1.0)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "0.6")
    private Float minConfidence = 0.6f;

    /**
     * Maximum detections to report (null for unlimited)
     */
    private Integer maxDetections;

    /**
     * Auto-capture photo on detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean autoCaptureOnDetection = true;

    /**
     * Investigate detected objects (move closer for better view)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean investigateDetections = false;

    /**
     * Investigation distance in meters (closer approach to detected object)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10.0")
    private Float investigationDistance = 10.0f;

    /**
     * Investigation duration per object in seconds
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10")
    private Integer investigationDuration = 10;

    /**
     * Gimbal pitch for detection in degrees
     */
    @Builder.Default
    @JsonProperty(defaultValue = "-45")
    private Integer gimbalPitch = -45;

    /**
     * Enable zoom during detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean enableZoom = false;

    /**
     * Zoom level for detection
     */
    @Builder.Default
    @JsonProperty(defaultValue = "1.0")
    private Float zoomLevel = 1.0f;

    /**
     * Maximum detection duration in seconds (null for unlimited)
     */
    private Integer maxDuration;

    /**
     * Action on max detections reached (continue, return_home, land, hover)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "return_home")
    private String onMaxDetectionsAction = "return_home";

    /**
     * Send real-time alerts on detection
     */
    @Builder.Default
    private Boolean realtimeAlerts = true;

    /**
     * Detection AI model identifier (optional, for custom models)
     */
    private String aiModelId;

    /**
     * Additional detection parameters (e.g., size filters, color filters)
     */
    @Builder.Default
    private List<DetectionParameter> detectionParameters = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetectionParameter {
        private String name;
        private String value;
        private String description;
    }

    @Override
    public String getConfigType() {
        return TaskTypeProto.TASK_TYPE_DETECT.name();
    }

    @Override
    public TaskTypeProto getTaskType() {
        return TaskTypeProto.TASK_TYPE_DETECT;
    }

    @Override
    public void validate() {
        if (detectionTargets == null || detectionTargets.isEmpty()) {
            throw new IllegalArgumentException("At least one detection target is required");
        }

        if (areaRadius != null && (areaRadius <= 0 || areaRadius > 1000)) {
            throw new IllegalArgumentException("Area radius must be between 0 and 1000 meters");
        }

        if (detectionAltitude != null && (detectionAltitude <= 0 || detectionAltitude > 500)) {
            throw new IllegalArgumentException("Detection altitude must be between 0 and 500 meters");
        }

        if (scanSpeed != null && (scanSpeed <= 0 || scanSpeed > 15)) {
            throw new IllegalArgumentException("Scan speed must be between 0 and 15 m/s");
        }

        if (minConfidence != null && (minConfidence < 0.0f || minConfidence > 1.0f)) {
            throw new IllegalArgumentException("Min confidence must be between 0.0 and 1.0");
        }

        if (investigationDistance != null && (investigationDistance < 5 || investigationDistance > 100)) {
            throw new IllegalArgumentException("Investigation distance must be between 5 and 100 meters");
        }

        if (gimbalPitch != null && (gimbalPitch < -90 || gimbalPitch > 30)) {
            throw new IllegalArgumentException("Gimbal pitch must be between -90 and 30 degrees");
        }

        if (zoomLevel != null && (zoomLevel < 1.0f || zoomLevel > 200.0f)) {
            throw new IllegalArgumentException("Zoom level must be between 1x and 200x");
        }

        if (!visualDetection && !thermalDetection) {
            throw new IllegalArgumentException("At least one detection method (visual or thermal) must be enabled");
        }
    }

    @Override
    public TaskConfigTemplate copy() {
        return this.toBuilder()
                .detectionTargets(new ArrayList<>(this.detectionTargets))
                .detectionParameters(new ArrayList<>(this.detectionParameters))
                .build();
    }
}
