package com.zqnt.utils.topics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ZequentTopics {

	ASSET_OSD_TOPIC("asset/{}/osd"), ASSET_TASK_TOPIC("asset/{}/task"), ASSET_TASK_REPLY_TOPIC(
			"asset/{}/task-reply"), ASSET_DETECTION_TOPIC("asset/detection"), ASSET_DRC_DOWN_TOPIC(
			"asset/{}/drc-down"), ASSET_DRC_UP_TOPIC("asset/{}/drc-up"), ASSET_EVENT_TOPIC("asset/{}/events"),

	ASSET_SERVICE_REQUEST_TOPIC("service/asset-service/request"), ASSET_SERVICE_REPLY_TOPIC(
			"service/asset-service/response"), MISSION_SERVICE_REQUEST_TOPIC(
			"service/mission-service/request"), MISSION_SERVICE_REPLY_TOPIC(
			"service/mission-service/response"), SCHEDULER_SERVICE_REQUEST_TOPIC(
			"service/scheduler-service/request"), SCHEDULER_SERVICE_REPLY_TOPIC(
			"service/scheduler-service/response"),
	EVENTS_REPLY("thing/product/{}/events_reply"), DRC_DOWN("thing/product/{}/drc/down"),
	REQUESTS_REPLY("thing/product/{}/requests_reply"), SERVICES("thing/product/{}/services"), SERVICES_REPLY(
			"thing/product/{}/services_reply"), 	STATUS_REPLY("sys/product/{}/status_reply");

	final String topic;

}
