package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.mission.proto.VehicleAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Waypoint}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaypointDTO implements Serializable {
    private Double latitude;
    private Double longitude;
    private Float altitude;
    private Float speed;
    private Boolean flyThrough;
    private VehicleAction vehicleAction;
	private Integer wpOrder;
	private Integer gimbalPitch;
}
