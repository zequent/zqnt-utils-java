package com.zqnt.utils.missionautonomy.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoFlyZoneDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String name;
    private String description;
    private String geoJson;
    private Double minimumAltitude;
    private Double maximumAltitude;
    private Boolean active;
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("No-fly zone name must be specified");
        }
        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalArgumentException("No-fly zone GeoJSON must be specified");
        }
        if (minimumAltitude != null && maximumAltitude != null
                && minimumAltitude > maximumAltitude) {
            throw new IllegalArgumentException(
                    "No-fly zone minimum altitude must not exceed maximum altitude");
        }
    }
}
