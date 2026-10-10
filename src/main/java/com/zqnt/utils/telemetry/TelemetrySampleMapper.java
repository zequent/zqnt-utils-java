package com.zqnt.utils.telemetry;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import com.google.protobuf.util.Timestamps;
import com.zqnt.protos.capability.v3.TelemetryField;
import com.zqnt.protos.capability.v3.TelemetryValueType;
import com.zqnt.protos.common.v3.AssetRef;
import com.zqnt.protos.common.v3.GeoPoint;
import com.zqnt.protos.telemetry.v3.TelemetrySample;
import com.zqnt.utils.common.proto.RequestBase;
import com.zqnt.utils.livedata.proto.ProduceTelemetryRequest;
import com.zqnt.utils.livedata.proto.SubAssetTelemetryDetails;
import com.zqnt.utils.livedata.proto.Telemetry;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * v2 {@link Telemetry} and v3 {@link TelemetrySample}, both ways. The fields every asset shares are
 * typed on the sample; every other v2 field is a {@code details} entry under one of the keys below,
 * enum values as their v2 name without the enum's prefix. {@link #telemetryFields()} describes those
 * keys for {@code CapabilitySet.telemetry_fields}.
 *
 * <p>Toward v2 a sample is a dock's ({@code asset}) or an aircraft's ({@code sub_asset}) by the
 * keys it carries; keys of the other kind, and keys v2 has no field for, are dropped there. v3
 * watchers get the sample as published. {@code Telemetry.id} (a per-sample UUID) and
 * {@code SubAssetTelemetryDetails.component_telemetry} are not carried.</p>
 *
 * <p>Depends on the generated contracts only, so an adapter can report the same fields.</p>
 */
public final class TelemetrySampleMapper {

    public static final String WIND_SPEED = "wind.speed";
    public static final String WIND_DIRECTION = "wind.direction";

    public static final String DOCK_ENVIRONMENT_TEMPERATURE = "dock.environment_temperature";
    public static final String DOCK_INSIDE_TEMPERATURE = "dock.inside_temperature";
    public static final String DOCK_HUMIDITY = "dock.humidity";
    public static final String DOCK_MODE = "dock.mode";
    public static final String DOCK_RAINFALL = "dock.rainfall";
    public static final String DOCK_COVER_STATE = "dock.cover_state";
    public static final String DOCK_DEBUG_MODE_OPEN = "dock.debug_mode_open";
    public static final String DOCK_POSITION_VALID = "dock.position_valid";
    public static final String DOCK_WORKING_VOLTAGE = "dock.working_voltage";
    public static final String DOCK_WORKING_CURRENT = "dock.working_current";
    public static final String DOCK_SUPPLY_VOLTAGE = "dock.supply_voltage";
    public static final String DOCK_AIR_CONDITIONER_STATE = "dock.air_conditioner.state";
    public static final String DOCK_AIR_CONDITIONER_SWITCH_TIME = "dock.air_conditioner.switch_time";
    public static final String DOCK_MANUAL_CONTROL_STATE = "dock.manual_control.state";
    public static final String DOCK_MANUAL_CONTROL_ACTIVE_SESSION = "dock.manual_control.active_session";
    public static final String DOCK_DRONE_SN = "dock.drone.sn";
    public static final String DOCK_DRONE_MODEL = "dock.drone.model";
    public static final String DOCK_DRONE_PAIRED = "dock.drone.paired";
    public static final String DOCK_DRONE_ONLINE = "dock.drone.online";
    public static final String DOCK_DRONE_AT_HOME = "dock.drone.at_home";
    public static final String DOCK_DRONE_CHARGING = "dock.drone.charging";
    public static final String DOCK_GNSS_GPS_SATELLITES = "dock.gnss.gps_satellites";
    public static final String DOCK_GNSS_RTK_SATELLITES = "dock.gnss.rtk_satellites";
    public static final String DOCK_GNSS_QUALITY = "dock.gnss.quality";
    public static final String NETWORK_TYPE = "network.type";
    public static final String NETWORK_RATE = "network.rate";
    public static final String NETWORK_QUALITY = "network.quality";
    public static final String LINK_4G_FREQUENCY_BAND = "link.4g.frequency_band";
    public static final String LINK_4G_GROUND_QUALITY = "link.4g.ground_quality";
    public static final String LINK_4G_LINK_STATE = "link.4g.link_state";
    public static final String LINK_4G_QUALITY = "link.4g.quality";
    public static final String LINK_4G_AIRCRAFT_QUALITY = "link.4g.aircraft_quality";
    public static final String LINK_DONGLE_COUNT = "link.dongle_count";
    public static final String LINK_WORK_MODE = "link.work_mode";
    public static final String LINK_SDR_FREQUENCY_BAND = "link.sdr.frequency_band";
    public static final String LINK_SDR_LINK_STATE = "link.sdr.link_state";
    public static final String LINK_SDR_QUALITY = "link.sdr.quality";
    public static final String SDR_DOWN_QUALITY = "sdr.down_quality";
    public static final String SDR_UP_QUALITY = "sdr.up_quality";
    public static final String SDR_FREQUENCY_BAND = "sdr.frequency_band";

    public static final String DRONE_MODE = "drone.mode";
    public static final String DRONE_GEAR = "drone.gear";
    public static final String DRONE_HEIGHT_LIMIT = "drone.height_limit";
    public static final String DRONE_HOME_DISTANCE = "drone.home_distance";
    public static final String DRONE_TOTAL_FLIGHT_DISTANCE = "drone.total_flight_distance";
    public static final String DRONE_TOTAL_FLIGHT_TIME = "drone.total_flight_time";
    public static final String DRONE_COUNTRY = "drone.country";
    public static final String DRONE_BATTERY_REMAINING_TIME = "drone.battery.remaining_time";
    public static final String DRONE_BATTERY_RETURN_TO_HOME_POWER = "drone.battery.return_to_home_power";
    public static final String PAYLOAD_ID = "payload.id";
    public static final String PAYLOAD_NAME = "payload.name";
    public static final String PAYLOAD_OBSERVED_AT = "payload.observed_at";
    public static final String CAMERA_CURRENT_LENS = "camera.current_lens";
    public static final String CAMERA_GIMBAL_PITCH = "camera.gimbal_pitch";
    public static final String CAMERA_GIMBAL_YAW = "camera.gimbal_yaw";
    public static final String CAMERA_GIMBAL_ROLL = "camera.gimbal_roll";
    public static final String CAMERA_ZOOM_FACTOR = "camera.zoom_factor";
    public static final String RANGE_FINDER_TARGET_LATITUDE = "range_finder.target_latitude";
    public static final String RANGE_FINDER_TARGET_LONGITUDE = "range_finder.target_longitude";
    public static final String RANGE_FINDER_TARGET_DISTANCE = "range_finder.target_distance";
    public static final String RANGE_FINDER_TARGET_ALTITUDE = "range_finder.target_altitude";
    public static final String SENSOR_TARGET_TEMPERATURE = "sensor.target_temperature";

    private enum Source { NONE, ASSET, SUB_ASSET }

    private record Detail(String key, List<FieldDescriptor> path, Source source, String unit, String description) {
    }

    private static final List<Detail> DETAILS = List.of(
            detail(WIND_SPEED, "wind_speed", "m/s", "Wind speed"),
            detail(DOCK_ENVIRONMENT_TEMPERATURE, "asset.environment_temp", "°C", "Temperature outside the dock"),
            detail(DOCK_INSIDE_TEMPERATURE, "asset.inside_temp", "°C", "Temperature inside the dock"),
            detail(DOCK_HUMIDITY, "asset.humidity", "%", "Relative humidity inside the dock"),
            detail(DOCK_MODE, "asset.mode", "", "Operating mode of the dock"),
            detail(DOCK_RAINFALL, "asset.rainfall", "", "Rainfall at the dock"),
            detail(DOCK_COVER_STATE, "asset.cover_state", "", "State of the dock cover"),
            detail(DOCK_DEBUG_MODE_OPEN, "asset.debug_mode_open", "", "Whether remote debugging is open"),
            detail(DOCK_POSITION_VALID, "asset.position_valid", "", "Whether the dock's position is calibrated"),
            detail(DOCK_WORKING_VOLTAGE, "asset.working_voltage", "mV", "Working voltage"),
            detail(DOCK_WORKING_CURRENT, "asset.working_current", "mA", "Working current"),
            detail(DOCK_SUPPLY_VOLTAGE, "asset.supply_voltage", "V", "Supply voltage"),
            detail(DOCK_AIR_CONDITIONER_STATE, "asset.air_conditioner.state", "", "Air conditioner state"),
            detail(DOCK_AIR_CONDITIONER_SWITCH_TIME, "asset.air_conditioner.switch_time", "s",
                    "Time until the air conditioner may switch mode again"),
            detail(DOCK_MANUAL_CONTROL_STATE, "asset.manual_control_state", "", "Manual control link state"),
            detail(DOCK_MANUAL_CONTROL_ACTIVE_SESSION, "asset.has_active_manual_control_session", "",
                    "Whether a manual control session is active"),
            detail(DOCK_DRONE_SN, "asset.sub_asset_information.sn", "", "Serial of the docked aircraft"),
            detail(DOCK_DRONE_MODEL, "asset.sub_asset_information.model", "", "Model of the docked aircraft"),
            detail(DOCK_DRONE_PAIRED, "asset.sub_asset_information.paired", "", "Whether the aircraft is paired"),
            detail(DOCK_DRONE_ONLINE, "asset.sub_asset_information.online", "", "Whether the aircraft is online"),
            detail(DOCK_DRONE_AT_HOME, "asset.sub_asset_at_home", "", "Whether the aircraft is in the dock"),
            detail(DOCK_DRONE_CHARGING, "asset.sub_asset_charging", "", "Whether the aircraft is charging"),
            detail(DOCK_GNSS_GPS_SATELLITES, "asset.position_state.gps_number", "", "GPS satellites in view"),
            detail(DOCK_GNSS_RTK_SATELLITES, "asset.position_state.rtk_number", "", "RTK satellites in view"),
            detail(DOCK_GNSS_QUALITY, "asset.position_state.quality", "", "Positioning quality level"),
            detail(NETWORK_TYPE, "asset.network_information.type", "", "Uplink network type"),
            detail(NETWORK_RATE, "asset.network_information.rate", "KB/s", "Uplink network rate"),
            detail(NETWORK_QUALITY, "asset.network_information.quality", "", "Uplink network quality"),
            detail(LINK_4G_FREQUENCY_BAND, "asset.wireless_link.fourth_generation_freq_band", "",
                    "4G link frequency band"),
            detail(LINK_4G_GROUND_QUALITY, "asset.wireless_link.fourth_generation_gnd_quality", "",
                    "4G link quality, ground side"),
            detail(LINK_4G_LINK_STATE, "asset.wireless_link.fourth_generation_link_state", "", "Whether the 4G link is up"),
            detail(LINK_4G_QUALITY, "asset.wireless_link.fourth_generation_quality", "", "4G link quality"),
            detail(LINK_4G_AIRCRAFT_QUALITY, "asset.wireless_link.fourth_generation_uav_quality", "",
                    "4G link quality, aircraft side"),
            detail(LINK_DONGLE_COUNT, "asset.wireless_link.dongle_number", "", "4G dongles in use"),
            detail(LINK_WORK_MODE, "asset.wireless_link.link_workmode", "", "Wireless link work mode"),
            detail(LINK_SDR_FREQUENCY_BAND, "asset.wireless_link.sdr_freq_band", "GHz", "SDR link frequency band"),
            detail(LINK_SDR_LINK_STATE, "asset.wireless_link.sdr_link_state", "", "Whether the SDR link is up"),
            detail(LINK_SDR_QUALITY, "asset.wireless_link.sdr_quality", "", "SDR link quality"),
            detail(SDR_DOWN_QUALITY, "asset.sdr_state.down_quality", "", "SDR downlink quality"),
            detail(SDR_UP_QUALITY, "asset.sdr_state.up_quality", "", "SDR uplink quality"),
            detail(SDR_FREQUENCY_BAND, "asset.sdr_state.frequency_band", "GHz", "SDR frequency band"),
            detail(WIND_DIRECTION, "sub_asset.wind_direction", "", "Wind direction"),
            detail(DRONE_MODE, "sub_asset.mode", "", "Flight mode of the aircraft"),
            detail(DRONE_GEAR, "sub_asset.gear", "", "Flight gear"),
            detail(DRONE_HEIGHT_LIMIT, "sub_asset.height_limit", "m", "Height limit"),
            detail(DRONE_HOME_DISTANCE, "sub_asset.home_distance", "m", "Distance to the home point"),
            detail(DRONE_TOTAL_FLIGHT_DISTANCE, "sub_asset.total_movement_distance", "m", "Total flight distance"),
            detail(DRONE_TOTAL_FLIGHT_TIME, "sub_asset.total_movement_time", "s", "Total flight time"),
            detail(DRONE_COUNTRY, "sub_asset.country", "", "Country the aircraft is in"),
            detail(DRONE_BATTERY_REMAINING_TIME, "sub_asset.battery_information.remaining_time", "s",
                    "Remaining flight time on the battery"),
            detail(DRONE_BATTERY_RETURN_TO_HOME_POWER, "sub_asset.battery_information.return_to_home_power", "",
                    "Battery needed to return home"),
            detail(PAYLOAD_ID, "sub_asset.payload_telemetry.id", "", "Payload id"),
            detail(PAYLOAD_NAME, "sub_asset.payload_telemetry.name", "", "Payload name"),
            detail(PAYLOAD_OBSERVED_AT, "sub_asset.payload_telemetry.timestamp", "", "When the payload reported (RFC 3339)"),
            detail(CAMERA_CURRENT_LENS, "sub_asset.payload_telemetry.camera_data.current_lens", "", "Active camera lens"),
            detail(CAMERA_GIMBAL_PITCH, "sub_asset.payload_telemetry.camera_data.gimbal_pitch", "°", "Gimbal pitch"),
            detail(CAMERA_GIMBAL_YAW, "sub_asset.payload_telemetry.camera_data.gimbal_yaw", "°", "Gimbal yaw"),
            detail(CAMERA_GIMBAL_ROLL, "sub_asset.payload_telemetry.camera_data.gimbal_roll", "°", "Gimbal roll"),
            detail(CAMERA_ZOOM_FACTOR, "sub_asset.payload_telemetry.camera_data.zoom_factor", "", "Camera zoom factor"),
            detail(RANGE_FINDER_TARGET_LATITUDE, "sub_asset.payload_telemetry.range_finder_data.target_latitude", "°",
                    "Latitude of the range finder's target"),
            detail(RANGE_FINDER_TARGET_LONGITUDE, "sub_asset.payload_telemetry.range_finder_data.target_longitude", "°",
                    "Longitude of the range finder's target"),
            detail(RANGE_FINDER_TARGET_DISTANCE, "sub_asset.payload_telemetry.range_finder_data.target_distance", "m",
                    "Distance to the range finder's target"),
            detail(RANGE_FINDER_TARGET_ALTITUDE, "sub_asset.payload_telemetry.range_finder_data.target_altitude", "m",
                    "Altitude of the range finder's target"),
            detail(SENSOR_TARGET_TEMPERATURE, "sub_asset.payload_telemetry.sensor_data.target_temperature", "°C",
                    "Temperature of the measured target"));

    private static final List<TelemetryField> TELEMETRY_FIELDS = DETAILS.stream()
            .map(TelemetrySampleMapper::describe).toList();

    private TelemetrySampleMapper() {
    }

    /** The keys {@link #toSample} writes into {@code details}, for {@code CapabilitySet.telemetry_fields}. */
    public static List<TelemetryField> telemetryFields() {
        return TELEMETRY_FIELDS;
    }

    /** The sample of a v2 frame, or null when the frame carries no telemetry. */
    public static TelemetrySample toSample(ProduceTelemetryRequest request) {
        return request.hasData() ? toSample(request.getData(), request.getBase().getSn()) : null;
    }

    public static TelemetrySample toSample(Telemetry telemetry, String fallbackSn) {
        String sn = telemetry.getSn().isBlank() ? fallbackSn : telemetry.getSn();
        TelemetrySample.Builder sample = TelemetrySample.newBuilder().setAsset(AssetRef.newBuilder().setSn(sn));
        if (telemetry.hasTimestamp()) sample.setObservedAt(telemetry.getTimestamp());
        if (telemetry.hasLatitude() && telemetry.hasLongitude()
                && Double.isFinite(telemetry.getLatitude()) && Double.isFinite(telemetry.getLongitude())) {
            GeoPoint.Builder position = GeoPoint.newBuilder()
                    .setLatitude(telemetry.getLatitude()).setLongitude(telemetry.getLongitude());
            if (telemetry.hasAbsoluteAltitude()) position.setAltitude(widen(telemetry.getAbsoluteAltitude()));
            sample.setPosition(position);
        }
        if (telemetry.hasRelativeAltitude()) sample.setRelativeAltitude(widen(telemetry.getRelativeAltitude()));
        if (telemetry.hasHeading()) sample.setHeadingDegrees(widen(telemetry.getHeading()));
        if (telemetry.hasSubAsset()) {
            SubAssetTelemetryDetails aircraft = telemetry.getSubAsset();
            if (aircraft.hasHorizontalSpeed()) sample.setHorizontalSpeed(widen(aircraft.getHorizontalSpeed()));
            if (aircraft.hasVerticalSpeed()) sample.setVerticalSpeed(widen(aircraft.getVerticalSpeed()));
            if (aircraft.getBatteryInformation().hasPercentage()) {
                parseNumber(aircraft.getBatteryInformation().getPercentage()).ifPresent(sample::setBatteryPercent);
            }
        } else if (telemetry.hasAsset() && telemetry.getAsset().hasSubAssetPercentage()) {
            sample.setBatteryPercent(widen(telemetry.getAsset().getSubAssetPercentage()));
        }
        Struct.Builder details = Struct.newBuilder();
        for (Detail detail : DETAILS) {
            Value value = read(telemetry, detail.path());
            if (value != null) details.putFields(detail.key(), value);
        }
        if (details.getFieldsCount() > 0) sample.setDetails(details);
        return sample.build();
    }

    /** The v2 frame of a sample, as an adapter would have streamed it. */
    public static ProduceTelemetryRequest toRequest(TelemetrySample sample) {
        RequestBase.Builder base = RequestBase.newBuilder()
                .setTid(UUID.randomUUID().toString())
                .setSn(sample.getAsset().getSn())
                .setTimestamp(sample.getObservedAt());
        if (!sample.getAsset().getId().isBlank()) base.setAssetId(sample.getAsset().getId());
        return ProduceTelemetryRequest.newBuilder()
                .setBase(base)
                .setData(toTelemetry(sample).toBuilder().setId(UUID.randomUUID().toString()))
                .build();
    }

    public static Telemetry toTelemetry(TelemetrySample sample) {
        Telemetry.Builder telemetry = Telemetry.newBuilder().setSn(sample.getAsset().getSn());
        if (sample.hasObservedAt()) telemetry.setTimestamp(sample.getObservedAt());
        if (sample.hasPosition()) {
            telemetry.setLatitude(sample.getPosition().getLatitude()).setLongitude(sample.getPosition().getLongitude());
            if (sample.getPosition().hasAltitude()) telemetry.setAbsoluteAltitude((float) sample.getPosition().getAltitude());
        }
        if (sample.hasRelativeAltitude()) telemetry.setRelativeAltitude((float) sample.getRelativeAltitude());
        if (sample.hasHeadingDegrees()) telemetry.setHeading((float) sample.getHeadingDegrees());

        Map<String, Value> details = sample.getDetails().getFieldsMap();
        Source source = sourceOf(sample, details);
        for (Detail detail : DETAILS) {
            Value value = details.get(detail.key());
            if (value != null && (detail.source() == Source.NONE || detail.source() == source)) {
                write(telemetry, detail.path(), value);
            }
        }
        if (source == Source.SUB_ASSET) {
            SubAssetTelemetryDetails.Builder aircraft = telemetry.getSubAssetBuilder();
            if (sample.hasHorizontalSpeed()) aircraft.setHorizontalSpeed((float) sample.getHorizontalSpeed());
            if (sample.hasVerticalSpeed()) aircraft.setVerticalSpeed((float) sample.getVerticalSpeed());
            if (sample.hasBatteryPercent()) {
                aircraft.getBatteryInformationBuilder().setPercentage(formatNumber(sample.getBatteryPercent()));
            }
        } else if (source == Source.ASSET && sample.hasBatteryPercent()) {
            telemetry.getAssetBuilder().setSubAssetPercentage((float) sample.getBatteryPercent());
        }
        return telemetry.build();
    }

    /** An aircraft's sample moves; a dock's or a single device's carries its own keys or only a battery. */
    private static Source sourceOf(TelemetrySample sample, Map<String, Value> details) {
        if (sample.hasHorizontalSpeed() || sample.hasVerticalSpeed() || carries(details, Source.SUB_ASSET)) {
            return Source.SUB_ASSET;
        }
        if (carries(details, Source.ASSET) || sample.hasBatteryPercent()) {
            return Source.ASSET;
        }
        return Source.NONE;
    }

    private static boolean carries(Map<String, Value> details, Source source) {
        return DETAILS.stream().anyMatch(detail -> detail.source() == source && details.containsKey(detail.key()));
    }

    private static Value read(Message message, List<FieldDescriptor> path) {
        Message current = message;
        for (FieldDescriptor step : path.subList(0, path.size() - 1)) {
            if (!current.hasField(step)) return null;
            current = (Message) current.getField(step);
        }
        FieldDescriptor leaf = path.getLast();
        return current.hasField(leaf) ? toValue(leaf, current.getField(leaf)) : null;
    }

    private static void write(Message.Builder root, List<FieldDescriptor> path, Value value) {
        FieldDescriptor leaf = path.getLast();
        Object converted = fromValue(leaf, value);
        if (converted == null) return;
        Message.Builder current = root;
        for (FieldDescriptor step : path.subList(0, path.size() - 1)) {
            current = current.getFieldBuilder(step);
        }
        current.setField(leaf, converted);
    }

    private static Value toValue(FieldDescriptor field, Object value) {
        return switch (field.getJavaType()) {
            case FLOAT -> number(widen((Float) value));
            case DOUBLE -> number((Double) value);
            case INT, LONG -> number(((Number) value).doubleValue());
            case BOOLEAN -> Value.newBuilder().setBoolValue((Boolean) value).build();
            case STRING -> Value.newBuilder().setStringValue((String) value).build();
            case ENUM -> Value.newBuilder().setStringValue(enumName((EnumValueDescriptor) value)).build();
            case MESSAGE -> value instanceof Timestamp timestamp
                    ? Value.newBuilder().setStringValue(Timestamps.toString(timestamp)).build() : null;
            default -> null;
        };
    }

    private static Object fromValue(FieldDescriptor field, Value value) {
        boolean isNumber = value.getKindCase() == Value.KindCase.NUMBER_VALUE;
        boolean isString = value.getKindCase() == Value.KindCase.STRING_VALUE;
        return switch (field.getJavaType()) {
            case FLOAT -> isNumber ? (float) value.getNumberValue() : null;
            case DOUBLE -> isNumber ? value.getNumberValue() : null;
            case INT -> isNumber ? (int) Math.round(value.getNumberValue()) : null;
            case LONG -> isNumber ? Math.round(value.getNumberValue()) : null;
            case BOOLEAN -> value.getKindCase() == Value.KindCase.BOOL_VALUE ? value.getBoolValue() : null;
            case STRING -> isString ? value.getStringValue() : null;
            case ENUM -> isString ? enumValue(field.getEnumType(), value.getStringValue()) : null;
            case MESSAGE -> isString && field.getMessageType().equals(Timestamp.getDescriptor())
                    ? parseTimestamp(value.getStringValue()) : null;
            default -> null;
        };
    }

    private static Value number(double value) {
        return Double.isFinite(value) ? Value.newBuilder().setNumberValue(value).build() : null;
    }

    /** A float as the decimal it was written as (0.1f is 0.1, not 0.10000000149011612). */
    private static double widen(float value) {
        return Double.parseDouble(Float.toString(value));
    }

    private static Optional<Double> parseNumber(String text) {
        try {
            double value = Double.parseDouble(text.strip());
            return Double.isFinite(value) ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) && Math.abs(value) < 1e15 ? Long.toString((long) value) : Double.toString(value);
    }

    private static Timestamp parseTimestamp(String text) {
        try {
            return Timestamps.parse(text);
        } catch (ParseException unreadable) {
            return null;
        }
    }

    private static String enumName(EnumValueDescriptor value) {
        return value.getName().substring(enumPrefix(value.getType()).length());
    }

    private static EnumValueDescriptor enumValue(EnumDescriptor type, String name) {
        EnumValueDescriptor value = type.findValueByName(enumPrefix(type) + name);
        return value != null ? value : type.findValueByName(name);
    }

    /** What every value name of an enum starts with, up to an underscore: {@code COVER_STATE_}. */
    private static String enumPrefix(EnumDescriptor type) {
        String prefix = type.getValues().getFirst().getName();
        for (EnumValueDescriptor value : type.getValues()) {
            while (!value.getName().startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        return prefix.substring(0, prefix.lastIndexOf('_') + 1);
    }

    private static TelemetryField describe(Detail detail) {
        FieldDescriptor leaf = detail.path().getLast();
        TelemetryField.Builder field = TelemetryField.newBuilder()
                .setKey(detail.key()).setUnit(detail.unit()).setDescription(detail.description());
        switch (leaf.getJavaType()) {
            case FLOAT, DOUBLE, INT, LONG -> field.setType(TelemetryValueType.TELEMETRY_VALUE_TYPE_NUMBER);
            case BOOLEAN -> field.setType(TelemetryValueType.TELEMETRY_VALUE_TYPE_BOOLEAN);
            case ENUM -> {
                field.setType(TelemetryValueType.TELEMETRY_VALUE_TYPE_STRING);
                leaf.getEnumType().getValues().forEach(value -> field.addAllowedValues(enumName(value)));
            }
            default -> field.setType(TelemetryValueType.TELEMETRY_VALUE_TYPE_STRING);
        }
        return field.build();
    }

    private static Detail detail(String key, String v2Path, String unit, String description) {
        List<FieldDescriptor> path = new ArrayList<>();
        Descriptor type = Telemetry.getDescriptor();
        for (String name : v2Path.split("\\.")) {
            FieldDescriptor field = type.findFieldByName(name);
            if (field == null || field.isRepeated()) {
                throw new IllegalStateException("No singular field " + name + " on the path " + v2Path);
            }
            path.add(field);
            type = field.getJavaType() == FieldDescriptor.JavaType.MESSAGE ? field.getMessageType() : null;
        }
        Source source = switch (path.getFirst().getName()) {
            case "asset" -> Source.ASSET;
            case "sub_asset" -> Source.SUB_ASSET;
            default -> Source.NONE;
        };
        return new Detail(key, List.copyOf(path), source, unit, description);
    }
}
