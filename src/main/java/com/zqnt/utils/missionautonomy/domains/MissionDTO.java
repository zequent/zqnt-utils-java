package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.MissionStatus;
import com.zqnt.utils.common.proto.MissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
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
    private Set<String> assignedAssets = new HashSet<>();
    private String updatedUser;

    /**
     * Validates this mission DTO.
     * @throws IllegalArgumentException if the mission is invalid
     */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mission name must be specified");
        }
    }
}