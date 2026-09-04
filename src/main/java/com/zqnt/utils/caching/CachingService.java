package com.zqnt.utils.caching;

import com.zqnt.utils.common.proto.AssetMode;
import com.zqnt.utils.common.proto.AssetProtoDTO;
import com.zqnt.utils.common.proto.AssetTypeEnum;
import com.zqnt.utils.common.proto.SubAssetProtoDTO;
import com.zqnt.utils.core.EdgeEndpointDTO;
import com.zqnt.utils.devicecontrol.proto.LiveStreamState;
import com.zqnt.utils.devicecontrol.proto.ManualControlRequest;
import com.zqnt.utils.devicecontrol.proto.ManualControlState;
import com.zqnt.utils.livedata.proto.Telemetry;
import com.zqnt.utils.mission.proto.TaskProtoDTO;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

public interface CachingService extends CacheOperations {

	Logger LOG = LoggerFactory.getLogger(CachingService.class);

	long DEFAULT_DTO_CACHE_TTL_SECONDS = 600L;

	static <T> Uni<T> handleRedisFailure(Uni<T> operation, String operationName, T fallbackValue) {
		if (operation == null) {
			LOG.warn("Redis operation '{}' was not created", operationName);
			return Uni.createFrom().item(fallbackValue);
		}
		return operation
				.onFailure().invoke(throwable -> LOG.error("Redis operation '{}' failed: {}", operationName,
						throwable.getMessage(), throwable))
				.onFailure().recoverWithItem(fallbackValue);
	}

	static Uni<Void> handleRedisVoidFailure(Uni<Void> operation, String operationName) {
		if (operation == null) {
			LOG.warn("Redis operation '{}' was not created", operationName);
			return Uni.createFrom().voidItem();
		}
		return operation
				.onFailure().invoke(throwable -> LOG.error("Redis operation '{}' failed: {}", operationName,
						throwable.getMessage(), throwable))
				.onFailure().recoverWithNull();
	}

	static <T> Uni<T> handleSerializationFailure(String operationName, Throwable throwable, T fallbackValue) {
		LOG.error("Cache serialization operation '{}' failed: {}", operationName, throwable.getMessage(), throwable);
		return Uni.createFrom().item(fallbackValue);
	}

	default <T> Uni<T> getOrFetch(String cacheName, Supplier<Uni<T>> cacheLookup, Supplier<Uni<T>> fallbackLookup,
			Function<T, Uni<Void>> cacheWriter) {
		return getOrFetch(cacheName, cacheLookup, fallbackLookup, cacheWriter, null);
	}

	default <T> Uni<T> getOrFetch(String cacheName, Supplier<Uni<T>> cacheLookup, Supplier<Uni<T>> fallbackLookup,
			Function<T, Uni<Void>> cacheWriter, T fallbackValue) {
		return safeLookup(cacheLookup, "cache lookup [" + cacheName + "]", fallbackValue)
				.onItem().ifNull().switchTo(() -> safeLookup(fallbackLookup, "fallback lookup [" + cacheName + "]",
						fallbackValue)
						.onItem().ifNotNull().call(item -> safeWrite(cacheWriter, item, cacheName)));
	}

	private static <T> Uni<T> safeLookup(Supplier<Uni<T>> lookup, String operationName, T fallbackValue) {
		if (lookup == null) {
			LOG.warn("Cache {} supplier is missing", operationName);
			return Uni.createFrom().item(fallbackValue);
		}
		try {
			return handleRedisFailure(lookup.get(), operationName, fallbackValue);
		} catch (RuntimeException e) {
			LOG.error("Cache {} failed before creating Redis operation: {}", operationName, e.getMessage(), e);
			return Uni.createFrom().item(fallbackValue);
		}
	}

	private static <T> Uni<Void> safeWrite(Function<T, Uni<Void>> cacheWriter, T item, String cacheName) {
		if (cacheWriter == null) {
			return Uni.createFrom().voidItem();
		}
		try {
			return handleRedisVoidFailure(cacheWriter.apply(item), "cache write [" + cacheName + "]");
		} catch (RuntimeException e) {
			LOG.error("Cache write [{}] failed before creating Redis operation: {}", cacheName, e.getMessage(), e);
			return Uni.createFrom().voidItem();
		}
	}

	Uni<Void> registerAssetVendor(String sn, String vendor);

	Uni<String> getAssetVendor(String sn);

	Uni<Void> registerEdgeEndpoint(String vendor, EdgeEndpointDTO endpoint);

	Uni<EdgeEndpointDTO> getEdgeEndpoint(String vendor);

	Uni<Void> deregisterEdgeEndpoint(String vendor);

	Uni<Void> deleteEdgeEndpoint(String vendor);

	Uni<Void> setAssetOnline(String deviceSn);

	// Task-caching methods below are implemented as defaults purely on top of CacheOperations
	// (get/set/delete/keys) so every implementer gets them for free from CacheKeys.ASSET_ACTIVE_TASKS
	// / ASSET_TASK_EXTERNAL_ID_REFERENCE — no Redis-specific access required. Override if a service
	// needs a different storage strategy.
	default Uni<Void> addActiveTaskToAsset(String sn, String taskId, TaskProtoDTO taskProtoDTO) {
		return set(CacheKeys.ASSET_ACTIVE_TASKS.getKeyPrefix()
				.replace("{sn}", sn)
				.replace("{taskId}", taskId), com.zqnt.utils.core.ProtoJsonUtils.toJson(taskProtoDTO));
	}

	default Uni<TaskProtoDTO> hasAnyAssetActiveTask(String sn) {
		String pattern = CacheKeys.ASSET_ACTIVE_TASKS.getKeyPrefix().replace("{sn}", sn).replace("{taskId}", "*");
		return keys(pattern).flatMap(matchingKeys -> {
			if (matchingKeys.isEmpty()) return Uni.createFrom().nullItem();
			return get(matchingKeys.iterator().next()).map(json -> {
				if (json == null || json.isBlank()) return null;
				return (TaskProtoDTO) com.zqnt.utils.core.ProtoJsonUtils.fromJson(json, TaskProtoDTO.newBuilder());
			});
		});
	}

	default Uni<Void> assignedTaskIsCompleted(String sn, String taskId) {
		return delete(CacheKeys.ASSET_ACTIVE_TASKS.getKeyPrefix()
				.replace("{sn}", sn)
				.replace("{taskId}", taskId)).replaceWithVoid();
	}

	default Uni<Void> setAssetTaskExternalIdReference(String externalId, String sn, TaskProtoDTO taskProtoDTO) {
		return set(CacheKeys.ASSET_TASK_EXTERNAL_ID_REFERENCE.getKeyPrefix()
				.replace("{externalId}", externalId)
				.replace("{sn}", sn), com.zqnt.utils.core.ProtoJsonUtils.toJson(taskProtoDTO));
	}

	default Uni<TaskProtoDTO> getAssetTaskWithExternalIdReference(String externalId, String sn) {
		return get(CacheKeys.ASSET_TASK_EXTERNAL_ID_REFERENCE.getKeyPrefix()
				.replace("{externalId}", externalId)
				.replace("{sn}", sn)).map(json -> {
			if (json == null || json.isBlank()) return null;
			return (TaskProtoDTO) com.zqnt.utils.core.ProtoJsonUtils.fromJson(json, TaskProtoDTO.newBuilder());
		});
	}

	Uni<AssetMode> getAssetMode(String deviceSn);

	Uni<Void> setSubAssetAtHome(String sn, Boolean isAtHome);

	Uni<Void> setCurrentTelemetry(String deviceSn, Telemetry telemetry);

	Uni<Void> setAssetSubAssetReferenceToAsset(String subAssetSn, String assetSn);

	Uni<String> getSubAssetReferenceToAsset(String subAssetSn);

	Uni<Telemetry> getCurrentTelemetry(String deviceSn);

	Uni<Void> setWaitingForServicesReply(String tid, String command, Object data);

	Uni<Boolean> isWaitingReplyExisting(String tid, String command);

	Uni<Void> setManualControlRequest(String sn, ManualControlRequest manualControlRequest);

	Uni<Void> setManualControlState(String sn, ManualControlState manualControlState);

	Uni<ManualControlState> getManualControlState(String sn);

	Uni<ManualControlRequest> getManualControlRequest(String sn);

	Uni<Integer> setManualControlRequestReplyReceived(String sn);

	Uni<Integer> setTransactionReplyReceived(String tid, String command);

	Uni<Void> setAssetLiveStreamState(String sn, String videoId, AssetTypeEnum assetType, Boolean isLive,
			Boolean hasStarted, String liveStreamUrl);

	Uni<Boolean> isSnSubAsset(String sn);

	Uni<Void> setAssetLiveStreamOffline(String sn);

	Uni<LiveStreamState> getAssetLiveStreamState(String sn);

	Uni<AssetProtoDTO> getAssetProtoDTO(String sn);

	Uni<SubAssetProtoDTO> getSubAssetProtoDTO(String sn);

	Uni<Void> setAssetProtoDTO(AssetProtoDTO assetProtoDTO, String sn);

	Uni<Void> setSubAssetProtoDTO(SubAssetProtoDTO subAssetProtoDTO, String sn);

	default Uni<AssetProtoDTO> getOrFetchAsset(String sn, Supplier<Uni<AssetProtoDTO>> connectorFallback) {
		return getOrFetch("asset:" + sn, () -> getAssetProtoDTO(sn), connectorFallback,
				asset -> setAssetProtoDTOWithTTL(asset, sn, DEFAULT_DTO_CACHE_TTL_SECONDS));
	}

	default Uni<SubAssetProtoDTO> getOrFetchSubAsset(String sn, Supplier<Uni<SubAssetProtoDTO>> connectorFallback) {
		return getOrFetch("subasset:" + sn, () -> getSubAssetProtoDTO(sn), connectorFallback,
				subAsset -> setSubAssetProtoDTOWithTTL(subAsset, sn, DEFAULT_DTO_CACHE_TTL_SECONDS));
	}

	Uni<Void> setAssetProtoDTOWithTTL(AssetProtoDTO assetProtoDTO, String sn, long ttlSeconds);

	Uni<Void> setSubAssetProtoDTOWithTTL(SubAssetProtoDTO subAssetProtoDTO, String sn, long ttlSeconds);
}
