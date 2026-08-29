package com.zqnt.utils.missionautonomy.domains.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.zqnt.utils.mission.proto.TaskTypeProto;

import java.io.Serializable;

/**
 * Base interface for all task configuration templates.
 * Implementations define type-specific configuration requirements.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "configType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DetectTaskConfig.class, name = "TASK_TYPE_DETECT"),
        @JsonSubTypes.Type(value = AreaMappingTaskConfig.class, name = "TASK_TYPE_AREA_MAPPING"),
        @JsonSubTypes.Type(value = WaypointTaskConfig.class, name = "TASK_TYPE_WAYPOINT"),
        @JsonSubTypes.Type(value = PoiTaskConfig.class, name = "TASK_TYPE_POI"),
        @JsonSubTypes.Type(value = FollowTaskConfig.class, name = "TASK_TYPE_FOLLOW"),
        @JsonSubTypes.Type(value = TrackTaskConfig.class, name = "TASK_TYPE_TRACK"),
})
public interface TaskConfigTemplate extends Serializable {

    /**
     * Returns the type name used as config discriminator (for JSON/binding).
     */
    String getConfigType();


    /**
     * Returns the task type this configuration belongs to
     */
    TaskTypeProto getTaskType();

    /**
     * Validates the configuration
     * @throws IllegalArgumentException if configuration is invalid
     */
    void validate();

    /**
     * Returns a deep copy of this configuration
     */
    TaskConfigTemplate copy();
}
