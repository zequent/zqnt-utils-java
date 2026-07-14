package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.AutonomyMode;
import com.zqnt.utils.common.proto.DecisionStrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutonomyConfigDTO implements Serializable {
    private Boolean enabled;
    private AutonomyMode mode;
    private DecisionStrategyType strategyType;
    private DynamicConfigDTO dynamicConfig;

    @Builder.Default
    private List<DecisionRuleDTO> decisionRules = new ArrayList<>();

    private String policyScope;
    private String policyScopeTarget;
    private Boolean requireHumanApproval;
    private Float minConfidence;
    private String fallbackAction;
}
