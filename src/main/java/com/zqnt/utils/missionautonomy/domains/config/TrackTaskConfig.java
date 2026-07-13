package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zqnt.utils.common.proto.TaskTypeProto;
import lombok.*;

/**
 * Configuration template for track target actions.
 * Locks onto and tracks a target with camera/gimbal while maintaining position.
 * Unlike FOLLOW, TRACK is stationary or limited movement with focus on target tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TrackTaskConfig implements TaskConfigTemplate {

    /**
     * Target identifier/type to track (REQUIRED)
     * e.g., "person", "vehicle", "object", "marker"
     */
    @NonNull
    private String targetType;

    /**
     * Initial target position - latitude
     */
    private Double initialLatitude;

    /**
     * Initial target position - longitude
     */
    private Double initialLongitude;

    /**
     * Target altitude/height estimate in meters (for 3D tracking)
     */
    private Float targetAltitude;

    /**
     * Tracking mode (stationary, adaptive, limited_movement)
     * - stationary: Drone stays in place, only gimbal moves
     * - adaptive: Drone adjusts position slightly to maintain optimal view
     * - limited_movement: Drone can move within defined radius
     */
    @Builder.Default
    @JsonProperty(defaultValue = "stationary")
    private String trackingMode = "stationary";

    /**
     * Maximum movement radius in meters (for adaptive/limited_movement modes)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10.0")
    private Float maxMovementRadius = 10.0f;

    /**
     * Tracking altitude in meters (drone's position)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "20.0")
    private Float trackingAltitude = 20.0f;

    /**
     * Enable gimbal tracking (camera follows target)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean gimbalTracking = true;

    /**
     * Enable zoom to keep target in frame at consistent size
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean autoZoom = false;

    /**
     * Target zoom level (1x - 200x depending on camera)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "1.0")
    private Float zoomLevel = 1.0f;

    /**
     * Tracking sensitivity (low, medium, high)
     * Controls how quickly drone/gimbal responds to target movement
     */
    @Builder.Default
    @JsonProperty(defaultValue = "medium")
    private String trackingSensitivity = "medium";

    /**
     * Maximum tracking duration in seconds (null for unlimited)
     */
    private Integer maxDuration;

    /**
     * Lost target behavior (hover, return_to_home, land, search)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "search")
    private String lostTargetAction = "search";

    /**
     * Target lost timeout in seconds before executing lostTargetAction
     */
    @Builder.Default
    @JsonProperty(defaultValue = "15")
    private Integer lostTargetTimeout = 15;

    /**
     * Search pattern if target is lost (spiral, grid, last_known)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "last_known")
    private String searchPattern = "last_known";

    /**
     * Search duration in seconds before giving up
     */
    @Builder.Default
    @JsonProperty(defaultValue = "60")
    private Integer searchDuration = 60;

    /**
     * Enable continuous recording during tracking
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean continuousRecording = true;

    /**
     * Enable photo capture during tracking
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean photoCapture = false;

    /**
     * Photo capture interval in seconds (if photoCapture is enabled)
     */
    private Integer captureInterval;

    /**
     * Target detection confidence threshold (0.0 - 1.0)
     * Minimum confidence to consider target as detected
     */
    @Builder.Default
    @JsonProperty(defaultValue = "0.7")
    private Float confidenceThreshold = 0.7f;

    @Override
    public String getConfigType() {
        return TaskTypeProto.TASK_TYPE_TRACK.name();
    }

    @Override
    public TaskTypeProto getTaskType() {
        return TaskTypeProto.TASK_TYPE_TRACK;
    }

    @Override
    public void validate() {
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new IllegalArgumentException("Target type is required for track actions");
        }

        if (maxMovementRadius != null && (maxMovementRadius < 0 || maxMovementRadius > 100)) {
            throw new IllegalArgumentException("Max movement radius must be between 0 and 100 meters");
        }

        if (trackingAltitude != null && (trackingAltitude <= 0 || trackingAltitude > 500)) {
            throw new IllegalArgumentException("Tracking altitude must be between 0 and 500 meters");
        }

        if (zoomLevel != null && (zoomLevel < 1.0f || zoomLevel > 200.0f)) {
            throw new IllegalArgumentException("Zoom level must be between 1x and 200x");
        }

        if (maxDuration != null && maxDuration <= 0) {
            throw new IllegalArgumentException("Max duration must be positive");
        }

        if (lostTargetTimeout != null && (lostTargetTimeout < 5 || lostTargetTimeout > 300)) {
            throw new IllegalArgumentException("Lost target timeout must be between 5 and 300 seconds");
        }

        if (searchDuration != null && (searchDuration < 10 || searchDuration > 600)) {
            throw new IllegalArgumentException("Search duration must be between 10 and 600 seconds");
        }

        if (confidenceThreshold != null && (confidenceThreshold < 0.0f || confidenceThreshold > 1.0f)) {
            throw new IllegalArgumentException("Confidence threshold must be between 0.0 and 1.0");
        }
    }

    @Override
    public TaskConfigTemplate copy() {
        return this.toBuilder().build();
    }
}
