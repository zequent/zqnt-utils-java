package com.zqnt.utils.asset.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPayloadDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String modifiedFrom;
    private String externalId;
    private String externalType;
    private Integer slotIndex;
    private String name;
    private String serialNumber;
    private String kind;
    private String vendor;
    private String model;
    private String firmwareVersion;
    private String libraryVersion;
    @Builder.Default
    private Map<String, Object> capabilities = new HashMap<>();
    @Builder.Default
    private Map<String, Object> state = new HashMap<>();
    @Builder.Default
    private List<PayloadCommandDefinitionDTO> commands = new ArrayList<>();
    private Boolean active;
    private LocalDateTime lastSeenAt;

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Payload name must be specified");
        }
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("Payload kind must be specified");
        }
        commands.forEach(PayloadCommandDefinitionDTO::validate);
    }
}
