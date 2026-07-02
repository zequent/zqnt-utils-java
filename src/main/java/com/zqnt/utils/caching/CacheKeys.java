package com.zqnt.utils.caching;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKeys {

	ASSET_ONLINE("asset-online:"),
	ASSET_MODE("asset-mode:"),
	ASSET_TELEMETRY("asset-telemetry:"),
	ASSET_LINK_TELEMETRY("asset-link-telemetry:"),
	ASSET_EXTENDED_TELEMTRY("asset-extended-telemetry:"),
	SUBASSET_TELEMETRY("subasset-telemetry:"),
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



	private final String keyPrefix;

}
