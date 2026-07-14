package com.zqnt.utils.grpc;

import com.zqnt.utils.caching.CachingService;
import com.zqnt.utils.core.EdgeEndpointDTO;
import com.zqnt.utils.edge.sdk.proto.EdgeAdapterServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class GrpcEndpointRouter implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(GrpcEndpointRouter.class);

	private static final int DEFAULT_ENDPOINT_CACHE_MAX_SIZE = 10_000;
	private static final Duration DEFAULT_ENDPOINT_CACHE_TTL = Duration.ofMinutes(5);

	private final CachingService cachingService;
	private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();
	private final Map<String, EdgeAdapterServiceGrpc.EdgeAdapterServiceStub> stubCache = new ConcurrentHashMap<>();
	private final ExpiringCache<String, EdgeEndpointDTO> endpointCache;

	public GrpcEndpointRouter(CachingService cachingService) {
		this(cachingService, DEFAULT_ENDPOINT_CACHE_MAX_SIZE, DEFAULT_ENDPOINT_CACHE_TTL);
	}


	public GrpcEndpointRouter(CachingService cachingService, int endpointCacheMaxSize, Duration endpointCacheTtl) {
		this.cachingService = Objects.requireNonNull(cachingService, "cachingService must not be null");
		this.endpointCache = new ExpiringCache<>(endpointCacheMaxSize, endpointCacheTtl);
	}

	public Uni<EdgeEndpointDTO> getEndpointForAsset(String sn) {
		EdgeEndpointDTO cached = endpointCache.get(sn);
		if (isOnline(cached)) {
			return Uni.createFrom().item(cached);
		}

		return cachingService.getAssetVendor(sn)
				.onItem().ifNull().failWith(() ->
						new EndpointNotFoundException("No vendor mapping found for SN: " + sn))
				.flatMap(vendor -> cachingService.getEdgeEndpoint(vendor)
						.onItem().ifNull().failWith(() ->
								new EndpointNotFoundException("No gRPC endpoint found for vendor: " + vendor
										+ " (SN: " + sn + ")"))
						.onItem().invoke(endpoint -> {
							if (!isOnline(endpoint)) {
								LOG.warn("Edge endpoint for vendor {} is offline (SN: {})", vendor, sn);
							}
						})
						.map(endpoint -> {
							if (!isOnline(endpoint)) {
								throw new EndpointOfflineException("Edge endpoint for vendor " + vendor
										+ " is offline (SN: " + sn + ")");
							}
							endpointCache.put(sn, endpoint);
							return endpoint;
						}));
	}

	public Uni<EdgeAdapterServiceGrpc.EdgeAdapterServiceStub> getStubForAsset(String sn) {
		return getEndpointForAsset(sn)
				.onItem().ifNull().failWith(() ->
						new EndpointNotFoundException("No gRPC endpoint registered for SN: " + sn))
				.map(edgeEndpoint -> getStubForEndpoint(edgeEndpoint.getEndpoint()))
				.invoke(stub -> LOG.debug("Using EdgeAdapterService stub for SN: {}", sn));
	}

	public EdgeAdapterServiceGrpc.EdgeAdapterServiceStub getStubForEndpoint(String endpoint) {
		String normalizedEndpoint = normalizeEndpoint(endpoint);
		ManagedChannel channel = channelCache.computeIfAbsent(normalizedEndpoint, this::createChannel);

		return stubCache.computeIfAbsent(normalizedEndpoint, key -> {
			LOG.info("Creating EdgeAdapterService stub for endpoint: {}", key);
			return EdgeAdapterServiceGrpc.newStub(channel);
		});
	}

	public Uni<Void> removeChannel(String endpoint) {
		String normalizedEndpoint = normalizeEndpoint(endpoint);

		stubCache.remove(normalizedEndpoint);
		ManagedChannel channel = channelCache.remove(normalizedEndpoint);
		endpointCache.removeMatching(cachedEndpoint ->
				normalizedEndpoint.equals(normalizeEndpoint(cachedEndpoint.getEndpoint())));

		if (channel == null) {
			LOG.debug("No channel found for endpoint: {}", normalizedEndpoint);
			return Uni.createFrom().voidItem();
		}

		return Uni.createFrom().voidItem()
				.invoke(() -> shutdownChannel(normalizedEndpoint, channel));
	}

	public void clearEndpointCache() {
		endpointCache.clear();
	}

	public void clearEndpointCache(String sn) {
		endpointCache.remove(sn);
	}

	public int getActiveChannelCount() {
		return channelCache.size();
	}

	public int getActiveStubCount() {
		return stubCache.size();
	}

	public int getCachedEndpointCount() {
		return endpointCache.size();
	}

	public void shutdown() {
		LOG.info("Shutting down {} channels and {} stubs", channelCache.size(), stubCache.size());
		stubCache.clear();
		endpointCache.clear();

		channelCache.forEach(this::shutdownChannel);
		channelCache.clear();
	}

	@Override
	public void close() {
		shutdown();
	}

	protected ManagedChannel createChannel(String endpoint) {
		String normalized = normalizeEndpoint(endpoint);
		LOG.info("Creating new gRPC channel for edge adapter endpoint: {}", normalized);

		return ManagedChannelBuilder
				.forTarget(normalized)
				.usePlaintext()
				.defaultLoadBalancingPolicy("pick_first")
				.keepAliveTime(30, TimeUnit.SECONDS)
				.keepAliveTimeout(10, TimeUnit.SECONDS)
				.keepAliveWithoutCalls(true)
				.idleTimeout(60, TimeUnit.SECONDS)
				.maxInboundMessageSize(10 * 1024 * 1024)
				.maxInboundMetadataSize(8 * 1024)
				.enableRetry()
				.maxRetryAttempts(3)
				.build();
	}

	protected String normalizeEndpoint(String endpoint) {
		if (endpoint == null || endpoint.isBlank()) {
			throw new EndpointNotFoundException("Edge endpoint is blank");
		}
		return endpoint
				.replaceFirst("^grpc://", "")
				.replaceFirst("^grpcs://", "");
	}

	private static boolean isOnline(EdgeEndpointDTO endpoint) {
		return endpoint != null && Boolean.TRUE.equals(endpoint.getOnline());
	}

	private void shutdownChannel(String endpoint, ManagedChannel channel) {
		try {
			LOG.info("Shutting down channel for endpoint: {}", endpoint);
			channel.shutdown();
			if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
				LOG.warn("Channel {} did not terminate gracefully, forcing shutdown", endpoint);
				channel.shutdownNow();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Interrupted while shutting down channel: {}", endpoint, e);
		}
	}

	public static class EndpointNotFoundException extends RuntimeException {
		public EndpointNotFoundException(String message) {
			super(message);
		}
	}

	public static class EndpointOfflineException extends RuntimeException {
		public EndpointOfflineException(String message) {
			super(message);
		}
	}

	public static class EdgeEndpointUnavailableException extends RuntimeException {
		public EdgeEndpointUnavailableException(String message) {
			super(message);
		}
	}

	public static class EdgeAdapterServiceUnavailableException extends RuntimeException {
		public EdgeAdapterServiceUnavailableException(String message) {
			super(message);
		}
	}

	private static final class ExpiringCache<K, V> {

		private final int maxSize;
		private final Duration ttl;
		private final Map<K, CacheEntry<V>> values = new ConcurrentHashMap<>();

		private ExpiringCache(int maxSize, Duration ttl) {
			if (maxSize < 1) {
				throw new IllegalArgumentException("maxSize must be greater than zero");
			}
			if (ttl == null || ttl.isZero() || ttl.isNegative()) {
				throw new IllegalArgumentException("ttl must be positive");
			}
			this.maxSize = maxSize;
			this.ttl = ttl;
		}

		private V get(K key) {
			CacheEntry<V> entry = values.get(key);
			if (entry == null) {
				return null;
			}
			if (entry.expiresAt().isBefore(Instant.now())) {
				values.remove(key, entry);
				return null;
			}
			return entry.value();
		}

		private void put(K key, V value) {
			if (values.size() >= maxSize) {
				removeExpired();
			}
			if (values.size() >= maxSize) {
				removeOne();
			}
			values.put(key, new CacheEntry<>(value, Instant.now().plus(ttl)));
		}

		private void remove(K key) {
			values.remove(key);
		}

		private void removeMatching(java.util.function.Predicate<V> predicate) {
			values.entrySet().removeIf(entry -> predicate.test(entry.getValue().value()));
		}

		private void clear() {
			values.clear();
		}

		private int size() {
			removeExpired();
			return values.size();
		}

		private void removeExpired() {
			Instant now = Instant.now();
			values.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
		}

		private void removeOne() {
			Iterator<K> iterator = values.keySet().iterator();
			if (iterator.hasNext()) {
				values.remove(iterator.next());
			}
		}
	}

	private record CacheEntry<V>(V value, Instant expiresAt) {
	}
}
