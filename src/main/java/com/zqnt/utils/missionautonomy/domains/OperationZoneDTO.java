package com.zqnt.utils.missionautonomy.domains;

import java.util.UUID;

import com.zqnt.utils.common.proto.OperationZoneType;
import com.zqnt.utils.common.proto.ZoneEnforcementType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationZoneDTO {
    private UUID id;
    private String name;
    private OperationZoneType type;
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
