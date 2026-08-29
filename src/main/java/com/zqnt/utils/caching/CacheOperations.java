package com.zqnt.utils.caching;

import com.zqnt.utils.JsonUtils;
import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.Set;

public interface CacheOperations {

	Uni<String> get(String key);

	Uni<Void> set(String key, String value);

	Uni<Void> set(String key, String value, Duration ttl);

	Uni<Integer> delete(String key);

	Uni<Boolean> exists(String key);

	Uni<Set<String>> keys(String pattern);

	default Uni<String> get(CacheKeys cacheKey, Object... keyValues) {
		return get(cacheKey.key(keyValues));
	}

	default Uni<Void> set(CacheKeys cacheKey, String value, Object... keyValues) {
		return set(cacheKey.key(keyValues), value);
	}

	default Uni<Void> set(CacheKeys cacheKey, String value, Duration ttl, Object... keyValues) {
		return set(cacheKey.key(keyValues), value, ttl);
	}

	default Uni<Integer> delete(CacheKeys cacheKey, Object... keyValues) {
		return delete(cacheKey.key(keyValues));
	}

	default Uni<Boolean> exists(CacheKeys cacheKey, Object... keyValues) {
		return exists(cacheKey.key(keyValues));
	}

	default Uni<Set<String>> keys(CacheKeys cacheKey) {
		return keys(cacheKey.pattern());
	}

	default Uni<Void> setJson(String key, Object value) {
		return set(key, JsonUtils.toJson(value));
	}

	default Uni<Void> setJson(String key, Object value, Duration ttl) {
		return set(key, JsonUtils.toJson(value), ttl);
	}

	default <T> Uni<T> getJson(String key, Class<T> type) {
		return get(key).map(json -> JsonUtils.fromJson(json, type));
	}
}
