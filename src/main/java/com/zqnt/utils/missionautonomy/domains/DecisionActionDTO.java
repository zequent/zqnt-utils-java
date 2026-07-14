package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.DecisionActionType;
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
public class DecisionActionDTO implements Serializable {
    private DecisionActionType type;
    private String targetRef;

    @Builder.Default
    private Map<String, Object> params = new LinkedHashMap<>();
}
