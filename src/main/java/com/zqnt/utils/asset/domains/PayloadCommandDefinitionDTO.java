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
    private String command;
    private String vendorMethod;
    private String description;
    @Builder.Default
    private Map<String, Object> parameterSchema = new HashMap<>();
    private Boolean available;

    public void validate() {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Payload command must be specified");
        }
    }
}
