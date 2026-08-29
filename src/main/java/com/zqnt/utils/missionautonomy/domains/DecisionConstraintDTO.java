package com.zqnt.utils.missionautonomy.domains;

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
public class DecisionConstraintDTO implements Serializable {
    private String name;

    @Builder.Default
    private Map<String, Object> params = new LinkedHashMap<>();

    private String violationAction;
}
