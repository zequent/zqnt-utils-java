package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.OperationStatus;
import com.zqnt.utils.common.proto.OperationType;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String modifiedFrom;
    private String name;
    private String description;
    private OperationStatus status;
    private OperationType type;
    private String organizationId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private DynamicConfigDTO operationConfig;
    private AutonomyConfigDTO autonomyConfig;
    private String externalId;
    private String externalOperationType;
   

    @Builder.Default
    private Set<String> assignedAssets = new HashSet<>();

    @Builder.Default
    private List<MissionDTO> missions = new ArrayList<>();

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Operation name must be specified");
        }

        if (type == null) {
            throw new IllegalArgumentException("Operation type must be specified");
        }

        if (missions != null) {
            missions.forEach(MissionDTO::validate);
        }
    }
}
