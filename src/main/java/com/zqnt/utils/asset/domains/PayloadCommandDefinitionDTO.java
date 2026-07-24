package com.zqnt.utils.asset.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadCommandDefinitionDTO implements Serializable {
    private String type;
    private String vendorMethod;
    private String description;
    @Builder.Default
    private Map<String, Object> configSchema = new HashMap<>();
    private Boolean available;
    private String unavailableReason;
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    public void validate() {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Payload command type must be specified");
        }
        if (!type.contains(".")) {
            throw new IllegalArgumentException("Payload command type must be namespaced");
        }
    }
}
