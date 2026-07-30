package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zqnt.utils.mission.proto.TaskTypeProto;
import lombok.*;

/**
 * Configuration template for follow target actions.
 * Dynamically follows a moving target while maintaining distance and camera lock.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FollowTaskConfig implements TaskConfigTemplate {


    /**
     * Target identifier/type to follow (REQUIRED)
     * e.g., "person", "vehicle", "boat", "animal"
     */
    @NonNull
    private String targetType;

    /**
     * Initial target position - latitude (can be updated dynamically)
     */
    private Double initialLatitude;

    /**
     * Initial target position - longitude (can be updated dynamically)
     */
    private Double initialLongitude;

    /**
     * Follow distance in meters (distance to maintain from target)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "15.0")
    private Float followDistance = 15.0f;

    /**
     * Follow altitude relative to target in meters
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10.0")
    private Float relativeAltitude = 10.0f;

    /**
     * Maximum follow speed in m/s
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10.0")
    private Float maxSpeed = 10.0f;

    /**
     * Follow mode (behind, above, side, custom)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "behind")
    private String followMode = "behind";

    /**
     * Angle offset in degrees (relative to target direction of travel)
     * 0 = directly behind, 90 = right side, -90 = left side, 180 = in front
     */
    @Builder.Default
    @JsonProperty(defaultValue = "0")
    private Integer angleOffset = 0;

    /**
     * Enable obstacle avoidance during follow
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean obstacleAvoidance = true;

    /**
     * Maximum follow duration in seconds (null for unlimited)
     */
    private Integer maxDuration;

    /**
     * Maximum follow distance from start point in meters (geofence)
     */
    private Float maxDistanceFromStart;

    /**
     * Lost target behavior (hover, return_to_home, land)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "hover")
    private String lostTargetAction = "hover";

    /**
     * Target lost timeout in seconds before executing lostTargetAction
     */
    @Builder.Default
    @JsonProperty(defaultValue = "30")
    private Integer lostTargetTimeout = 30;

    /**
     * Keep camera locked on target
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean lockCameraOnTarget = true;

    /**
     * Gimbal pitch offset in degrees (relative to target lock)
     */
    @Builder.Default
    private Integer gimbalPitchOffset = -15;

    /**
     * Enable auto-capture during follow
     */
    @Builder.Default
    private Boolean autoCapture = false;

    /**
     * Capture interval in seconds (if autoCapture is enabled)
     */
    private Integer captureInterval;

    @Override
    public String getConfigType() {
        return TaskTypeProto.TASK_TYPE_FOLLOW.name();
    }

    @Override
    public TaskTypeProto getTaskType() {
        return TaskTypeProto.TASK_TYPE_FOLLOW;
    }

    @Override
    public void validate() {
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new IllegalArgumentException("Target type is required for follow actions");
        }

        if (followDistance != null && (followDistance < 5 || followDistance > 100)) {
            throw new IllegalArgumentException("Follow distance must be between 5 and 100 meters");
        }

        if (relativeAltitude != null && (relativeAltitude < 2 || relativeAltitude > 100)) {
            throw new IllegalArgumentException("Relative altitude must be between 2 and 100 meters");
        }

        if (maxSpeed != null && (maxSpeed <= 0 || maxSpeed > 20)) {
            throw new IllegalArgumentException("Max speed must be between 0 and 20 m/s");
        }

        if (angleOffset != null && (angleOffset < -180 || angleOffset > 180)) {
            throw new IllegalArgumentException("Angle offset must be between -180 and 180 degrees");
        }

        if (gimbalPitchOffset != null && (gimbalPitchOffset < -90 || gimbalPitchOffset > 30)) {
            throw new IllegalArgumentException("Gimbal pitch offset must be between -90 and 30 degrees");
        }

        if (maxDuration != null && maxDuration <= 0) {
            throw new IllegalArgumentException("Max duration must be positive");
        }

        if (lostTargetTimeout != null && (lostTargetTimeout < 5 || lostTargetTimeout > 300)) {
            throw new IllegalArgumentException("Lost target timeout must be between 5 and 300 seconds");
        }
    }

    @Override
    public TaskConfigTemplate copy() {
        return this.toBuilder().build();
    }
}
