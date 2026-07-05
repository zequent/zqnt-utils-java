package com.zqnt.utils.edge.sdk.domains;

import com.zqnt.utils.common.proto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * POJO representation of Asset (Dock) Telemetry data.
 * Maps to AssetTelemetry proto message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetTelemetryData {

	private String id;
	private LocalDateTime timestamp;
	private String sn;
	private Double latitude;
	private Double longitude;
	private Float absoluteAltitude;
	private Float relativeAltitude;
	private Float environmentTemp;
	private Float insideTemp;
	private Float humidity;
	private AssetMode mode;
	private RainfallEnum rainfall;
	private SubAssetInformation subAssetInformation;
	private Boolean subAssetAtHome;
	private Boolean subAssetCharging;
	private Float subAssetPercentage;
	private Float heading;
	private Boolean debugModeOpen;
	private Boolean hasActiveManualControlSession;
	private AssetCoverStateEnum coverState;
	private Integer workingVoltage;
	private Integer workingCurrent;
	private Integer supplyVoltage;
	private Float windSpeed;
	private Boolean positionValid;
	private NetworkInformation networkInformation;
	private WirelessLinkInformation wirelessLink;
	private SdrState sdrState;
	private AirConditioner airConditioner;
	private ManualControlStateEnum manualControlState;
	private PositionState positionState;


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AirConditioner {
		private AssetAirConditionerStateEnum state;
		private Integer switchTime;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NetworkInformation {
		private NetworkTypeEnum type;
		private Float rate;
		private NetworkStateQualityEnum quality;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WirelessLinkInformation {
		private Float fourthGenerationFreqBand;
		private Integer fourthGenerationGndQuality;
		private Boolean fourthGenerationLinkState;
		private Integer fourthGenerationQuality;
		private Integer fourthGenerationUavQuality;
		private Integer dongleNumber;
		private String linkWorkmode;
		private Float sdrFreqBand;
		private Boolean sdrLinkState;
		private Integer sdrQuality;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SubAssetInformation {
		private String sn;
		private String model;
		private Boolean paired;
		private Boolean online;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PositionState {
		private Integer gpsNumber;
		private Integer rtkNumber;
		private Integer quality;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SdrState {
		private Integer downQuality;
		private Double frequencyBand;
		private Integer upQuality;
	}

}
