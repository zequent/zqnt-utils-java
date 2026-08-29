package com.zqnt.utils.missionautonomy.domains;

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
public class DecisionRuleDTO implements Serializable {
    private String id;
    private String name;
    private Boolean enabled;
    private Integer priority;

    @Builder.Default
    private List<DecisionTriggerDTO> triggers = new ArrayList<>();

    @Builder.Default
    private List<DecisionConditionDTO> conditions = new ArrayList<>();

    @Builder.Default
    private List<DecisionConstraintDTO> constraints = new ArrayList<>();

    @Builder.Default
    private List<DecisionActionDTO> actions = new ArrayList<>();
}
