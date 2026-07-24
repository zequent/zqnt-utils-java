package com.zqnt.utils.edge.sdk.domains;

import com.zqnt.utils.common.proto.SubAssetMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * POJO representation of SubAsset (Drone) Telemetry data.
 * Maps to SubAssetTelemetry proto message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAssetTelemetryData {

	private String id;
	private LocalDateTime timestamp;
	private Double latitude;
	private Double longitude;
	private Float absoluteAltitude;
	private Float relativeAltitude;
	private Float horizontalSpeed;
	private Float verticalSpeed;
	private Float windSpeed;
	private String windDirection;
	private Float heading;
	private Integer gear;
	private PayloadTelemetry payloadTelemetry;
	private BatteryInformation batteryInformation;
	private Integer heightLimit;
	private Float homeDistance;
	private Double totalMovementDistance;
	private Double totalMovementTime;
	private SubAssetMode mode;
	private String country;
	private List<ComponentTelemetryData> componentTelemetry;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ComponentTelemetryData {
		private String componentId;
		private String externalId;
		private String kind;
		private LocalDateTime timestamp;
		private CameraData cameraData;
		private RangeFinderData rangeFinderData;
		private SensorData sensorData;
		private Map<String, Object> attributes;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BatteryInformation {
		private String percentage;
		private Integer remainingTime;
		private String returnToHomePower;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PayloadTelemetry {
		private String id;
		private LocalDateTime timestamp;
		private CameraData cameraData;
		private RangeFinderData rangeFinderData;
		private SensorData sensorData;
		private String name;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CameraData {
		private String currentLens;
		private Float gimbalPitch;
		private Float gimbalYaw;
		private Float gimbalRoll;
		private Float zoomFactor;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RangeFinderData {
		private Double targetLatitude;
		private Double targetLongitude;
		private Float targetDistance;
		private Float targetAltitude;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SensorData {
		private Float targetTemperature;
	}

}
