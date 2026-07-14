package com.zqnt.utils.grpc;

import io.grpc.stub.StreamObserver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MutinyUtils {

	private static final Logger LOG = LoggerFactory.getLogger(MutinyUtils.class);

	private MutinyUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static <REQ, RES> Uni<RES> callAsync(REQ request, BiConsumer<REQ, StreamObserver<RES>> grpcMethod) {
		Objects.requireNonNull(grpcMethod, "grpcMethod must not be null");
		return callAsync(responseObserver -> grpcMethod.accept(request, responseObserver));
	}

	public static <RES> Uni<RES> callAsync(Consumer<StreamObserver<RES>> grpcCall) {
		Objects.requireNonNull(grpcCall, "grpcCall must not be null");
		return Uni.createFrom().completionStage(() -> {
			CompletableFuture<RES> future = new CompletableFuture<>();
			try {
				grpcCall.accept(new SingleResponseObserver<>(future));
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
			return future;
		});
	}

	public static <T> void uniToObserver(Uni<T> uni, StreamObserver<T> responseObserver) {
		Objects.requireNonNull(uni, "uni must not be null");
		Objects.requireNonNull(responseObserver, "responseObserver must not be null");

		uni.subscribe().with(
				response -> {
					if (response != null) {
						responseObserver.onNext(response);
					}
					responseObserver.onCompleted();
				},
				error -> failObserver("Error in Uni processing", error, responseObserver)
		);
	}

	public static <T> void multiToObserver(Multi<T> multi, StreamObserver<T> responseObserver) {
		Objects.requireNonNull(multi, "multi must not be null");
		Objects.requireNonNull(responseObserver, "responseObserver must not be null");

		multi.subscribe().with(
				response -> {
					if (response != null) {
						responseObserver.onNext(response);
					}
				},
				error -> failObserver("Error in Multi processing", error, responseObserver),
				responseObserver::onCompleted
		);
	}

	private static <T> void failObserver(String message, Throwable error, StreamObserver<T> responseObserver) {
		LOG.error(message, error);
		try {
			responseObserver.onError(error);
		} catch (RuntimeException observerError) {
			LOG.error("Failed to notify gRPC observer about error", observerError);
		}
	}

	private static final class SingleResponseObserver<T> implements StreamObserver<T> {

		private final CompletableFuture<T> future;
		private T response;

		private SingleResponseObserver(CompletableFuture<T> future) {
			this.future = future;
		}

		@Override
		public void onNext(T value) {
			response = value;
		}

		@Override
		public void onError(Throwable throwable) {
			future.completeExceptionally(throwable);
		}

		@Override
		public void onCompleted() {
			future.complete(response);
		}
	}
}
