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
}
