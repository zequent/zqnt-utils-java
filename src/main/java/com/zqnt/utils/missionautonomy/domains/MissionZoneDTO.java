package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.MissionZoneType;
import com.zqnt.utils.common.proto.ZoneEnforcementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MissionZoneDTO {
    private UUID id;
    private String name;
    private MissionZoneType type;
    private ZoneEnforcementType enforcementType;
    private Boolean active;
    private Integer priority;
    private DynamicConfigDTO config;

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Zone name must be specified");
        }
        if (type == null) {
            throw new IllegalArgumentException("Zone type must be specified");
        }
        if (enforcementType == null) {
            throw new IllegalArgumentException("Zone enforcement type must be specified");
        }
    }
}
