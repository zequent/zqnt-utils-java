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

	// All keys live under the "zqnt:" namespace so a shared/multi-tenant Redis instance can tell our
	// keys apart at a glance (SCAN zqnt:*, ACL patterns, etc.) — matches the convention every other
	// Redis key in the platform already follows (pubsub channels, streams, licensing leases,
	// mission-autonomy's terrain/config/policy caches).
	ASSET_ONLINE("zqnt:asset-online:"),
	ASSET_MODE("zqnt:asset-mode:"),
	TELEMETRY("zqnt:telemetry:{sn}"),
	ASSET_LINK_TELEMETRY("zqnt:asset-link-telemetry:"),
	ASSET_EXTENDED_TELEMTRY("zqnt:asset-extended-telemetry:"),
	ASSET_MANUAL_CONTROL_STATE("zqnt:drc-state:"),
	ASSET_LIVE_STREAM_STATE("zqnt:live-stream-state:"),
	ASSET_SERVICES_REPLY_WAIT("zqnt:asset-task-reply-wait:{tid}:{method}"),
	ASSET_ACTIVE_TASKS("zqnt:asset-active-tasks:{sn}:{taskId}"),
	ASSET_COMPLETED_TASKS("zqnt:asset-completed-tasks:{sn}:{taskId}"),
	ASSET_TASK_EXTERNAL_ID_REFERENCE("zqnt:asset-task-external-id-reference:{externalId}:{sn}"),
	ASSET_MANUAL_CONTROL_REQUEST("zqnt:asset-manual-control-request:"),
	SUBASSET_AT_HOME("zqnt:subaset-at-home:"),
	ASSET_SUBASSET_REFERENCE("zqnt:asset-subasset-reference:"),
	ASSET_PROPERTIES("zqnt:asset-properties:"),
	ASSET_DTO("zqnt:asset-dto:{sn}"),
	SUBASSET_DTO("zqnt:subasset-dto:{subAssetSn}"),
	EDGE_ENDPOINTS("zqnt:edge-endpoints:{vendor}"),
	EDGE_VENDOR("zqnt:edge-vendor:{sn}"),  // Maps SN to vendor for routing
	ASSET_CURRENT_TASK("zqnt:asset-current-task:{sn}"),  // Current executing task for an asset (externalTaskId → JSON)
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
