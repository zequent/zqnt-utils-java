package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.DecisionTriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DecisionTriggerDTO implements Serializable {
    private DecisionTriggerType type;
    private String eventType;

    @Builder.Default
    private Map<String, Object> params = new LinkedHashMap<>();
}
