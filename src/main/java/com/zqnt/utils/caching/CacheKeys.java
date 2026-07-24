package com.zqnt.utils.caching;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public enum CacheKeys {

	ASSET_ONLINE("asset-online:"),
	ASSET_MODE("asset-mode:"),
	TELEMETRY("telemetry:{sn}"),
	ASSET_LINK_TELEMETRY("asset-link-telemetry:"),
	ASSET_EXTENDED_TELEMTRY("asset-extended-telemetry:"),
	ASSET_MANUAL_CONTROL_STATE("drc-state:"),
	ASSET_LIVE_STREAM_STATE("live-stream-state:"),
	ASSET_SERVICES_REPLY_WAIT("asset-task-reply-wait:{tid}:{method}"),
	ASSET_ACTIVE_TASKS("asset-active-tasks:{sn}:{taskId}"),
	ASSET_COMPLETED_TASKS("asset-completed-tasks:{sn}:{taskId}"),
	ASSET_TASK_EXTERNAL_ID_REFERENCE("asset-task-external-id-reference:{externalId}:{sn}"),
	ASSET_MANUAL_CONTROL_REQUEST("asset-manual-control-request:"),
	SUBASSET_AT_HOME("subaset-at-home:"),
	ASSET_SUBASSET_REFERENCE("asset-subasset-reference:"),
	ASSET_PROPERTIES("asset-properties:"),
	ASSET_DTO("asset-dto:{sn}"),
	SUBASSET_DTO("subasset-dto:{subAssetSn}"),
	EDGE_ENDPOINTS("edge-endpoints:{vendor}"),
	EDGE_VENDOR("edge-vendor:{sn}"),  // Maps SN to vendor for routing
	ASSET_CURRENT_TASK("asset-current-task:{sn}"),  // Current executing task for an asset (externalTaskId → JSON)
	;


	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

	private final String keyPrefix;

	public String key(Object... values) {
		Set<String> placeholders = placeholders();
		if (placeholders.isEmpty()) {
			return keyPrefix + join(values);
		}
		if (values.length != placeholders.size()) {
			throw new IllegalArgumentException(name() + " expects " + placeholders.size()
					+ " key value(s), but got " + values.length);
		}

		String key = keyPrefix;
		int index = 0;
		for (String placeholder : placeholders) {
			key = key.replace("{" + placeholder + "}", String.valueOf(values[index++]));
		}
		return key;
	}

	public String key(Map<String, ?> values) {
		String key = keyPrefix;
		for (String placeholder : placeholders()) {
			if (!values.containsKey(placeholder)) {
				throw new IllegalArgumentException(name() + " is missing key value: " + placeholder);
			}
			key = key.replace("{" + placeholder + "}", String.valueOf(values.get(placeholder)));
		}
		return key;
	}

	public String pattern() {
		return PLACEHOLDER_PATTERN.matcher(keyPrefix).replaceAll("*");
	}

	public Set<String> placeholders() {
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(keyPrefix);
		Set<String> placeholders = new LinkedHashSet<>();
		while (matcher.find()) {
			placeholders.add(matcher.group(1));
		}
		return placeholders;
	}

	private static String join(Object[] values) {
		StringBuilder builder = new StringBuilder();
		for (Object value : values) {
			builder.append(value);
		}
		return builder.toString();
	}

}
