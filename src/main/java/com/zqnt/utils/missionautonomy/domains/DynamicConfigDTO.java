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
public class DynamicConfigDTO implements Serializable {
    private String templateId;
    private String templateVersion;

    @Builder.Default
    private Map<String, Object> templateConfig = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Object> overrides = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Object> decisionConfig = new LinkedHashMap<>();

    private Boolean decisionEngineEnabled;
}
