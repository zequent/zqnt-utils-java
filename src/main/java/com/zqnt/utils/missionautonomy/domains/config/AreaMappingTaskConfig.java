package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zqnt.utils.common.proto.TaskTypeProto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration template for area mapping actions.
 * Supports polygon-based area coverage with automated photo capture.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AreaMappingTaskConfig implements TaskConfigTemplate {

    /**
     * Survey area polygon vertices (REQUIRED, minimum 3 points)
     */
    @NonNull
    @Builder.Default
    private List<AreaVertex> areaVertices = new ArrayList<>();

    /**
     * Survey altitude in meters (REQUIRED)
     */
    @NonNull
    private Float surveyAltitude;

    /**
     * Flight pattern (grid, zigzag, circular)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "grid")
    private String flightPattern = "grid";

    /**
     * Overlap percentage between photos (front overlap)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "70")
    private Integer frontOverlap = 70;

    /**
     * Side overlap percentage between flight lines
     */
    @Builder.Default
    @JsonProperty(defaultValue = "60")
    private Integer sideOverlap = 60;

    /**
     * Flight speed in m/s
     */
    @Builder.Default
    @JsonProperty(defaultValue = "5.0")
    private Float speed = 5.0f;

    /**
     * Gimbal pitch angle in degrees (typically -90 for nadir shots)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "-90")
    private Integer gimbalPitch = -90;

    /**
     * Camera angle in degrees (0 = nadir, 45 = oblique)
     */
    @Builder.Default
    @JsonProperty(defaultValue = "0")
    private Integer cameraAngle = 0;

    /**
     * Enable terrain following
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean terrainFollowing = false;

    /**
     * Ground sampling distance in cm/pixel (for planning)
     */
    private Float groundSamplingDistance;

    /**
     * Enable 3D model reconstruction
     */
    @Builder.Default
    @JsonProperty(defaultValue = "false")
    private Boolean enable3DReconstruction = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AreaVertex {
        private Double latitude;
        private Double longitude;
        private Integer order;
    }

    @Override
    public String getConfigType() {
        return TaskTypeProto.TASK_TYPE_AREA_MAPPING.name();
    }

    @Override
    public TaskTypeProto getTaskType() {
        return TaskTypeProto.TASK_TYPE_AREA_MAPPING;
    }

    @Override
    public void validate() {
        if (areaVertices == null || areaVertices.size() < 3) {
            throw new IllegalArgumentException("Area survey requires at least 3 vertices to define a polygon");
        }

        if (surveyAltitude == null || surveyAltitude <= 0 || surveyAltitude > 500) {
            throw new IllegalArgumentException("Survey altitude must be between 0 and 500 meters");
        }

        if (frontOverlap != null && (frontOverlap < 0 || frontOverlap > 95)) {
            throw new IllegalArgumentException("Front overlap must be between 0 and 95 percent");
        }

        if (sideOverlap != null && (sideOverlap < 0 || sideOverlap > 95)) {
            throw new IllegalArgumentException("Side overlap must be between 0 and 95 percent");
        }

        if (speed != null && (speed <= 0 || speed > 15)) {
            throw new IllegalArgumentException("Speed must be between 0 and 15 m/s");
        }

        if (gimbalPitch != null && (gimbalPitch < -90 || gimbalPitch > 30)) {
            throw new IllegalArgumentException("Gimbal pitch must be between -90 and 30 degrees");
        }
    }

    @Override
    public TaskConfigTemplate copy() {
        return this.toBuilder()
                .areaVertices(new ArrayList<>(this.areaVertices))
                .build();
    }
}
