package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.MissionStatus;
import com.zqnt.utils.common.proto.MissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link Mission}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MissionDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String modifiedFrom;
    private String name;
    private String description;
    private MissionStatus status;
    private MissionType type;
    private String geoJson;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private DynamicConfigDTO missionConfig;
    private AutonomyConfigDTO autonomyConfig;
    private String externalId;
    private String externalMissionType;

    @Builder.Default
    private Set<String> assignedAssets = new HashSet<>();
    @Builder.Default
    private List<TaskDTO> tasks = new ArrayList<>();
    @Builder.Default
    private List<MissionZoneDTO> zones = new ArrayList<>();

    /**
     * Validates this mission DTO.
     * 
     * @throws IllegalArgumentException if the mission is invalid
     */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mission name must be specified");
        }

        if (type == null) {
            throw new IllegalArgumentException("Mission type must be specified");
        }

        if (tasks != null) {
            tasks.forEach(TaskDTO::validate);
        }

        if (zones != null) {
            zones.forEach(MissionZoneDTO::validate);
        }
    }
}
