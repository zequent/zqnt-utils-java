package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.mission.proto.GeoAreaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoAreaDTO implements Serializable {
    private GeoAreaType type;
    @Builder.Default
    private List<GeoPointDTO> vertices = new ArrayList<>();
    private GeoPointDTO center;
    private Double radiusMeters;
    private String geoJson;

    public void validate() {
        if (type == null || type == GeoAreaType.GEO_AREA_TYPE_UNSPECIFIED
                || type == GeoAreaType.UNRECOGNIZED) {
            throw new IllegalArgumentException("Geo area type must be specified");
        }
        if (vertices == null) {
            throw new IllegalArgumentException("Geo area vertices must not be null");
        }
        for (int index = 0; index < vertices.size(); index++) {
            if (vertices.get(index) == null) {
                throw new IllegalArgumentException("Geo area vertex[" + index + "] must not be null");
            }
            vertices.get(index).validate();
        }

        switch (type) {
            case GEO_AREA_TYPE_POLYGON -> {
                if (vertices.size() < 3) {
                    throw new IllegalArgumentException("Polygon geo area requires at least three vertices");
                }
            }
            case GEO_AREA_TYPE_BOUNDING_BOX -> {
                if (vertices.size() != 2) {
                    throw new IllegalArgumentException("Bounding-box geo area requires exactly two vertices");
                }
            }
            case GEO_AREA_TYPE_CIRCLE -> {
                if (center == null) {
                    throw new IllegalArgumentException("Circle geo area requires a center");
                }
                center.validate();
                if (radiusMeters == null || !Double.isFinite(radiusMeters) || radiusMeters <= 0) {
                    throw new IllegalArgumentException("Circle geo area requires a positive radius");
                }
            }
            case GEO_AREA_TYPE_GEO_JSON -> {
                if (geoJson == null || geoJson.isBlank()) {
                    throw new IllegalArgumentException("GeoJSON geo area requires geoJson");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported geo area type: " + type);
        }
    }
}
