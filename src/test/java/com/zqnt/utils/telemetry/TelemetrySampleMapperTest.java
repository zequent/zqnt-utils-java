package com.zqnt.utils.telemetry;

import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import com.zqnt.protos.capability.v3.TelemetryField;
import com.zqnt.protos.capability.v3.TelemetryValueType;
import com.zqnt.protos.common.v3.AssetRef;
import com.zqnt.protos.common.v3.GeoPoint;
import com.zqnt.protos.telemetry.v3.TelemetrySample;
import com.zqnt.utils.common.proto.*;
import com.zqnt.utils.livedata.proto.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zqnt.utils.telemetry.TelemetrySampleMapper.*;
import static org.junit.jupiter.api.Assertions.*;

class TelemetrySampleMapperTest {

    private static final Timestamp AT = Timestamp.newBuilder().setSeconds(1_791_000_000L).setNanos(250_000_000).build();

    /** Every field of AssetTelemetryDetails set, the way the DJI adapter reports a dock. */
    static Telemetry dock() {
        return Telemetry.newBuilder()
                .setId("sample-1").setTimestamp(AT).setSn("DOCK-1")
                .setLatitude(52.5200).setLongitude(13.4050).setAbsoluteAltitude(34.5f)
                .setRelativeAltitude(0f).setWindSpeed(3.2f).setHeading(181.5f)
                .setAsset(AssetTelemetryDetails.newBuilder()
                        .setEnvironmentTemp(21.3f).setInsideTemp(25.1f).setHumidity(48f)
                        .setMode(AssetMode.ASSET_MODE_WORKING).setRainfall(RainfallEnum.RAINFALL_LIGHT)
                        .setSubAssetInformation(AssetTelemetryDetails.AssetSubAssetInformation.newBuilder()
                                .setSn("AIRCRAFT-1").setModel("M3TD").setPaired(true).setOnline(false))
                        .setSubAssetAtHome(true).setSubAssetCharging(true).setSubAssetPercentage(87f)
                        .setDebugModeOpen(false).setHasActiveManualControlSession(false)
                        .setCoverState(AssetCoverStateEnum.COVER_STATE_CLOSED)
                        .setWorkingVoltage(24_000).setWorkingCurrent(1_250).setSupplyVoltage(230)
                        .setPositionValid(true)
                        .setNetworkInformation(AssetTelemetryDetails.AssetNetworkInformation.newBuilder()
                                .setType(NetworkTypeEnum.NETWORK_TYPE_4_G).setRate(512.5f)
                                .setQuality(NetworkStateQualityEnum.NETWORK_STATE_QUALITY_GOOD))
                        .setAirConditioner(AssetTelemetryDetails.AssetAirConditioner.newBuilder()
                                .setState(AssetAirConditionerStateEnum.AIR_CONDITIONER_COOL).setSwitchTime(30))
                        .setManualControlState(ManualControlStateEnum.MANUAL_CONTROL_STATE_DISCONNECTED)
                        .setPositionState(AssetTelemetryDetails.PositionState.newBuilder()
                                .setGpsNumber(12).setRtkNumber(20).setQuality(5))
                        .setWirelessLink(AssetTelemetryDetails.AssetWirelessLinkInformation.newBuilder()
                                .setFourthGenerationFreqBand(1.8f).setFourthGenerationGndQuality(4)
                                .setFourthGenerationLinkState(true).setFourthGenerationQuality(3)
                                .setFourthGenerationUavQuality(2).setDongleNumber(1).setLinkWorkmode("SDR_AND_4G")
                                .setSdrFreqBand(5.8f).setSdrLinkState(true).setSdrQuality(5))
                        .setSdrState(AssetTelemetryDetails.AssetSdrState.newBuilder()
                                .setDownQuality(5).setUpQuality(4).setFrequencyBand(2.4)))
                .build();
    }

    /** An aircraft in flight with its camera, range finder and thermal sensor. */
    static Telemetry drone() {
        return Telemetry.newBuilder()
                .setId("sample-2").setTimestamp(AT).setSn("AIRCRAFT-1")
                .setLatitude(52.5210).setLongitude(13.4070).setAbsoluteAltitude(110.2f)
                .setRelativeAltitude(75.7f).setWindSpeed(5.1f).setHeading(-12.25f)
                .setSubAsset(SubAssetTelemetryDetails.newBuilder()
                        .setHorizontalSpeed(12.3f).setVerticalSpeed(-0.4f).setWindDirection("NORTH_EAST")
                        .setGear(1).setHeightLimit(120).setHomeDistance(842.6f)
                        .setTotalMovementDistance(123_456.7).setTotalMovementTime(98_765)
                        .setMode(SubAssetMode.SUBASSET_MODE_WAYLINE).setCountry("DE")
                        .setBatteryInformation(SubAssetTelemetryDetails.SubAssetBatteryInformation.newBuilder()
                                .setPercentage("64").setRemainingTime(1_320).setReturnToHomePower("22"))
                        .setPayloadTelemetry(PayloadTelemetry.newBuilder()
                                .setId("payload-0").setName("H20T").setTimestamp(AT)
                                .setCameraData(PayloadTelemetry.CameraData.newBuilder()
                                        .setCurrentLens("ir").setGimbalPitch(-45.5f).setGimbalYaw(10f)
                                        .setZoomFactor(2f).setGimbalRoll(0.1f))
                                .setRangeFinderData(PayloadTelemetry.RangeFinderData.newBuilder()
                                        .setTargetLatitude(52.53).setTargetLongitude(13.41)
                                        .setTargetDistance(150.5f).setTargetAltitude(30f))
                                .setSensorData(PayloadTelemetry.SensorData.newBuilder().setTargetTemperature(36.6f))))
                .build();
    }

    @Test
    void aDockSampleSurvivesTheRoundTrip() {
        Telemetry dock = dock();
        assertEquals(dock.toBuilder().clearId().build(), toTelemetry(toSample(dock, "")));
    }

    @Test
    void aDroneSampleSurvivesTheRoundTrip() {
        Telemetry drone = drone();
        assertEquals(drone.toBuilder().clearId().build(), toTelemetry(toSample(drone, "")));
    }

    @Test
    void theSharedFieldsAreTyped() {
        TelemetrySample dock = toSample(dock(), "");
        assertEquals(AssetRef.newBuilder().setSn("DOCK-1").build(), dock.getAsset());
        assertEquals(AT, dock.getObservedAt());
        assertEquals(GeoPoint.newBuilder().setLatitude(52.52).setLongitude(13.405).setAltitude(34.5).build(),
                dock.getPosition());
        assertEquals(181.5, dock.getHeadingDegrees());
        assertEquals(87, dock.getBatteryPercent(), "a dock's battery is its aircraft's, as the console shows it");
        assertFalse(dock.hasHorizontalSpeed());

        TelemetrySample drone = toSample(drone(), "");
        assertEquals(75.7, drone.getRelativeAltitude());
        assertEquals(12.3, drone.getHorizontalSpeed());
        assertEquals(-0.4, drone.getVerticalSpeed());
        assertEquals(64, drone.getBatteryPercent());
    }

    @Test
    void detailsUseTheDottedKeysAndEnumNamesWithoutPrefix() {
        Map<String, Value> dock = toSample(dock(), "").getDetails().getFieldsMap();
        assertEquals("CLOSED", dock.get(DOCK_COVER_STATE).getStringValue());
        assertEquals("WORKING", dock.get(DOCK_MODE).getStringValue());
        assertEquals("COOL", dock.get(DOCK_AIR_CONDITIONER_STATE).getStringValue());
        assertEquals("LIGHT", dock.get(DOCK_RAINFALL).getStringValue());
        assertEquals("4_G", dock.get(NETWORK_TYPE).getStringValue());
        assertEquals("GOOD", dock.get(NETWORK_QUALITY).getStringValue());
        assertEquals(21.3, dock.get(DOCK_ENVIRONMENT_TEMPERATURE).getNumberValue());
        assertEquals(5, dock.get(LINK_SDR_QUALITY).getNumberValue());
        assertTrue(dock.get(DOCK_DRONE_AT_HOME).getBoolValue());
        assertEquals(3.2, dock.get(WIND_SPEED).getNumberValue());

        Map<String, Value> drone = toSample(drone(), "").getDetails().getFieldsMap();
        assertEquals("WAYLINE", drone.get(DRONE_MODE).getStringValue());
        assertEquals(-45.5, drone.get(CAMERA_GIMBAL_PITCH).getNumberValue());
        assertEquals("2026-10-03T04:00:00.250Z", drone.get(PAYLOAD_OBSERVED_AT).getStringValue());
    }

    @Test
    void everyDetailKeyIsDescribedOnceWithItsType() {
        Set<String> written = toSample(dock(), "").getDetails().getFieldsMap().keySet();
        Set<String> aircraft = toSample(drone(), "").getDetails().getFieldsMap().keySet();
        Map<String, TelemetryField> described = telemetryFields().stream()
                .collect(Collectors.toMap(TelemetryField::getKey, field -> field));
        assertTrue(described.keySet().containsAll(written));
        assertTrue(described.keySet().containsAll(aircraft));
        assertEquals(described.size(), telemetryFields().size(), "no key twice");

        TelemetryField cover = described.get(DOCK_COVER_STATE);
        assertEquals(TelemetryValueType.TELEMETRY_VALUE_TYPE_STRING, cover.getType());
        assertEquals(List.of("CLOSED", "OPENED", "HALF_OPEN", "ABNORMAL"), cover.getAllowedValuesList());
        assertEquals(TelemetryValueType.TELEMETRY_VALUE_TYPE_NUMBER, described.get(DOCK_ENVIRONMENT_TEMPERATURE).getType());
        assertEquals("°C", described.get(DOCK_ENVIRONMENT_TEMPERATURE).getUnit());
        assertEquals(TelemetryValueType.TELEMETRY_VALUE_TYPE_BOOLEAN, described.get(DOCK_DRONE_CHARGING).getType());
    }

    @Test
    void keysV2HasNoFieldForAreDroppedThereAndTheSourceFollowsTheKeys() {
        TelemetrySample sample = TelemetrySample.newBuilder()
                .setAsset(AssetRef.newBuilder().setSn("RADAR-1")).setObservedAt(AT).setBatteryPercent(55.5)
                .setDetails(Struct.newBuilder()
                        .putFields("radar.mode", Value.newBuilder().setStringValue("SCAN").build())
                        .putFields(DOCK_COVER_STATE, Value.newBuilder().setStringValue("OPENED").build())
                        .putFields(DOCK_HUMIDITY, Value.newBuilder().setStringValue("not a number").build()))
                .build();
        Telemetry telemetry = toTelemetry(sample);
        assertTrue(telemetry.hasAsset());
        assertEquals(AssetCoverStateEnum.COVER_STATE_OPENED, telemetry.getAsset().getCoverState());
        assertFalse(telemetry.getAsset().hasHumidity(), "a value of the wrong type is not guessed at");
        assertEquals(55.5f, telemetry.getAsset().getSubAssetPercentage());

        Telemetry aircraft = toTelemetry(sample.toBuilder().setHorizontalSpeed(4).build());
        assertTrue(aircraft.hasSubAsset(), "a moving asset is an aircraft; the dock's keys are dropped");
        assertEquals("55.5", aircraft.getSubAsset().getBatteryInformation().getPercentage());
    }

    @Test
    void aSampleBecomesTheFrameAnAdapterWouldHaveStreamed() {
        TelemetrySample sample = toSample(drone(), "").toBuilder()
                .setAsset(AssetRef.newBuilder().setSn("AIRCRAFT-1").setId("aircraft-1-id")).build();
        ProduceTelemetryRequest request = toRequest(sample);
        assertEquals("AIRCRAFT-1", request.getBase().getSn());
        assertEquals("aircraft-1-id", request.getBase().getAssetId());
        assertEquals(AT, request.getBase().getTimestamp());
        assertFalse(request.getBase().getTid().isBlank());
        assertFalse(request.getData().getId().isBlank());
        assertEquals(sample, toSample(request).toBuilder().setAsset(sample.getAsset()).build());
    }

    @Test
    void aFrameWithoutCoordinatesOrSerialStillMaps() {
        Telemetry noPosition = Telemetry.newBuilder().setLatitude(Double.NaN).setLongitude(Double.NaN).build();
        TelemetrySample sample = toSample(noPosition, "FROM-BASE");
        assertFalse(sample.hasPosition(), "omitted coordinates arrive as NaN");
        assertEquals("FROM-BASE", sample.getAsset().getSn());
        assertNull(toSample(ProduceTelemetryRequest.getDefaultInstance()));
    }
}
