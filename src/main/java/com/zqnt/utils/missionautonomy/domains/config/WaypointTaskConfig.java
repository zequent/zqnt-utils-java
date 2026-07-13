package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zqnt.utils.common.proto.*;
import com.zqnt.utils.missionautonomy.domains.WaypointDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration template for waypoint-based flight tasks.
 * Contains all waypoint-specific parameters with defaults and validation rules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WaypointTaskConfig implements TaskConfigTemplate {

    // ============= REQUIRED FIELDS =============

    /**
     * External task identifier — the ID the asset/device uses for this execution.
     * Vendor-neutral: called "flightId" by DJI, "mission_id" by Mavlink, "route_id" by ground robots.
     * Used to correlate status callbacks from the asset back to this task.
     */
    @JsonAlias("flightId")  // backwards-compat for existing DB records
    private String externalTaskId;

    /**
     * Waypoints to follow (REQUIRED, minimum 2 waypoints)
     */
    @NonNull
    @Builder.Default
    private List<WaypointDTO> waypoints = new ArrayList<>();

    // ============= FLIGHT BEHAVIOR =============

    /**
     * Mode for flying to wayline start point
     */
    @Builder.Default
    @JsonProperty(defaultValue = "FTW_MODE_POINT_TO_POINT")
    private FlyToWaylineModeProto flyToWaylineMode = FlyToWaylineModeProto.FTW_MODE_POINT_TO_POINT;

    /**
     * Action when wayline is finished
     */
    @Builder.Default
    @JsonProperty(defaultValue = "WF_ACTION_GO_HOME")
    private WaylineFinishActionProto waylineFinishAction = WaylineFinishActionProto.WF_ACTION_GO_HOME;

    /**
     * Wayline type (straight, curved, etc.)
     */
    @JsonProperty(defaultValue = "WT_WAYPOINT")
    @Builder.Default
    private WaylineTypeEnumProto waylineType = WaylineTypeEnumProto.WT_WAYPOINT;

    /**
     * Turn mode between waypoints
     */
    @JsonProperty(defaultValue = "WT_MODE_TO_POINT_AND_PASS_WITH_CONTINUITY_CURVATURE")
    @Builder.Default
    private WaylineTurnModeProto waylineTurnMode = WaylineTurnModeProto.WT_MODE_TO_POINT_AND_PASS_WITH_CONTINUITY_CURVATURE;

    /**
     * Use straight line between waypoints
     */
    @Builder.Default
    @JsonProperty(defaultValue = "true")
    private Boolean useStraightLine = true;

    /**
     * Wayline precision type
     */
    @Builder.Default
    @JsonProperty(defaultValue = "PRECISION_GPS")
    private WaylinePrecisionTypeEnumProto waylinePrecisionType = WaylinePrecisionTypeEnumProto.PRECISION_GPS;

    // ============= SAFETY & FAILSAFE =============

    /**
     * Behavior when RC signal is lost during wayline
     */
    @Builder.Default
    @JsonProperty(defaultValue = "EWWRL_EXECUTE_RC_LOST_ACTION")
    private ExitWaylineWhenRcLostEnumProto exitWaylineWhenRcLostEnum = ExitWaylineWhenRcLostEnumProto.EWWRL_EXECUTE_RC_LOST_ACTION;

    /**
     * Action when RC is lost
     */
    @JsonProperty(defaultValue = "RC_LOST_ACTION_RETURN_HOME")
    @Builder.Default
    private RcLostActionEnumProto rcLostActionEnum = RcLostActionEnumProto.RC_LOST_ACTION_RETURN_HOME;

    /**
     * Action when out of control
     */
    @JsonProperty(defaultValue = "OOC_RETURN_TO_HOME")
    @Builder.Default
    private OutOfControlActionEnumProto outOfControlAction = OutOfControlActionEnumProto.OOC_RETURN_TO_HOME;

    /**
     * Take-off security height in meters
     */
    @Builder.Default
    @JsonProperty(defaultValue = "10.0")
    private Float takeOffSecurityHeight = 10.0f;

    // ============= RETURN TO HOME (RTH) =============

    /**
     * RTH altitude in meters (nullable, uses global default if null)
     */
    private Integer rthAltitude;

    /**
     * RTH mode (nullable, uses global default if null)
     */
    private RthModeEnumProto rthMode;

    /**
     * RTH speed in m/s (nullable, uses global default if null)
     */
    private Float rthSpeed;

    // ============= SPEED & MOVEMENT =============

    /**
     * Global speed for all waypoints in m/s (can be overridden per waypoint)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "5.0")
    private Float globalSpeed = 5.0f;

    /**
     * Global transition speed between waypoints in m/s
     */
    @JsonProperty(defaultValue = "8.0")
    @Builder.Default
    private Float globalTransitionSpeed = 8.0f;

    /**
     * Global height for all waypoints in meters (can be overridden per waypoint)
     */
    @JsonProperty(defaultValue = "50.0")
    @Builder.Default
    private Float globalHeight = 50.0f;

    // ============= GIMBAL CONTROL =============

    /**
     * Gimbal pitch control mode
     */
    @Builder.Default
    @JsonProperty(defaultValue = "WGP_MODE_LOOK_DOWN")
    private WaylineGimbalPitchModeProto gimbalPitchMode = WaylineGimbalPitchModeProto.WGP_MODE_LOOK_DOWN;

    /**
     * Global gimbal pitch in degrees (-90 to 0, where -90 is straight down)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "-45")
    private Integer globalGimbalPitch = -45;

    // ============= PAYLOAD & IMAGING =============

    /**
     * Payload imaging type (nullable, camera-specific)
     */
    private String payloadImagingType;

    // ============= FILE REFERENCES =============

    /**
     * KMZ/Wayline file URL (nullable, for pre-uploaded missions)
     */
    private String fileUrl;

    /**
     * MD5 checksum of the file (nullable)
     */
    private String fileMd5;

    /**
     * Flight area/geofence file URL (nullable)
     */
    private String flightAreaFileUrl;

    /**
     * Flight area file checksum (nullable)
     */
    private String flightAreaChecksum;

    // ============= VALIDATION & LIFECYCLE =============

    @Override
    public String getConfigType() {
        return TaskTypeProto.TASK_TYPE_WAYPOINT.name();
    }

    @Override
    public TaskTypeProto getTaskType() {
        return TaskTypeProto.TASK_TYPE_WAYPOINT;
    }

    @Override
    public void validate() {

        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalArgumentException("At least one waypoint is required");
        }

        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("Waypoint tasks require at least 2 waypoints");
        }

        if (globalSpeed != null && (globalSpeed <= 0 || globalSpeed > 15)) {
            throw new IllegalArgumentException("Global speed must be between 0 and 15 m/s");
        }

        if (globalHeight != null && (globalHeight < 0 || globalHeight > 500)) {
            throw new IllegalArgumentException("Global height must be between 0 and 500 meters");
        }

        if (globalGimbalPitch != null && (globalGimbalPitch < -90 || globalGimbalPitch > 30)) {
            throw new IllegalArgumentException("Global gimbal pitch must be between -90 and 30 degrees");
        }

        if (takeOffSecurityHeight != null && (takeOffSecurityHeight < 2 || takeOffSecurityHeight > 1500)) {
            throw new IllegalArgumentException("Take-off security height must be between 2 and 1500 meters");
        }
    }

    @Override
    public TaskConfigTemplate copy() {
        return this.toBuilder()
                .waypoints(new ArrayList<>(this.waypoints))
                .build();
    }
}
