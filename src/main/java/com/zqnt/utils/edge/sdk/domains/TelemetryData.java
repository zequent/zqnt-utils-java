package com.zqnt.utils.edge.sdk.domains;

import com.zqnt.utils.common.proto.AssetAirConditionerStateEnum;
import com.zqnt.utils.common.proto.AssetCoverStateEnum;
import com.zqnt.utils.common.proto.AssetMode;
import com.zqnt.utils.common.proto.ManualControlStateEnum;
import com.zqnt.utils.common.proto.NetworkStateQualityEnum;
import com.zqnt.utils.common.proto.NetworkTypeEnum;
import com.zqnt.utils.common.proto.RainfallEnum;
import com.zqnt.utils.common.proto.SubAssetMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified asset and sub-asset telemetry. Exactly one detail object must be set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryData {

    private String id;
    private LocalDateTime timestamp;
    private String sn;
    private Double latitude;
    private Double longitude;
    private Float absoluteAltitude;
    private Float relativeAltitude;
    private Float windSpeed;
    private Float heading;
    private AssetDetails asset;
    private SubAssetDetails subAsset;

    public SourceType getSourceType() {
        if (asset != null && subAsset == null) {
            return SourceType.ASSET;
        }
        if (subAsset != null && asset == null) {
            return SourceType.SUB_ASSET;
        }
        return SourceType.UNSPECIFIED;
    }

    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Telemetry ID must be specified");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Telemetry timestamp must be specified");
        }
        if (sn == null || sn.isBlank()) {
            throw new IllegalArgumentException("Telemetry serial number must be specified");
        }
        if ((asset == null) == (subAsset == null)) {
            throw new IllegalArgumentException("Exactly one telemetry source must be provided");
        }
    }

    public enum SourceType {
        UNSPECIFIED,
        ASSET,
        SUB_ASSET
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetDetails {
        private Float environmentTemp;
        private Float insideTemp;
        private Float humidity;
        private AssetMode mode;
        private RainfallEnum rainfall;
        private SubAssetInformation subAssetInformation;
        private Boolean subAssetAtHome;
        private Boolean subAssetCharging;
        private Float subAssetPercentage;
        private Boolean debugModeOpen;
        private Boolean hasActiveManualControlSession;
        private AssetCoverStateEnum coverState;
        private Integer workingVoltage;
        private Integer workingCurrent;
        private Integer supplyVoltage;
        private Boolean positionValid;
        private NetworkInformation networkInformation;
        private AirConditioner airConditioner;
        private ManualControlStateEnum manualControlState;
        private PositionState positionState;
        private WirelessLinkInformation wirelessLink;
        private SdrState sdrState;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubAssetDetails {
        private Float horizontalSpeed;
        private Float verticalSpeed;
        private String windDirection;
        private Integer gear;
        private PayloadTelemetry payloadTelemetry;
        private BatteryInformation batteryInformation;
        private Integer heightLimit;
        private Float homeDistance;
        private Double totalMovementDistance;
        private Double totalMovementTime;
        private SubAssetMode mode;
        private String country;
        @Builder.Default
        private List<ComponentTelemetryData> componentTelemetry = new ArrayList<>();
    }

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
        @Builder.Default
        private Map<String, Object> attributes = new HashMap<>();
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
    public static class BatteryInformation {
        private String percentage;
        private Integer remainingTime;
        private String returnToHomePower;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CameraData {
        private String currentLens;
        private Float gimbalPitch;
        private Float gimbalYaw;
        private Float zoomFactor;
        private Float gimbalRoll;
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
        private Integer upQuality;
        private Double frequencyBand;
    }
}
