package com.videotron.bsi.service.a.function;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ErrorCode;

import io.vertx.core.http.HttpClosedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Factories of the BsiException raised by client adapters and services. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BsiExceptionFunction {

    private static final Predicate<Throwable> isTimeout = failure -> causeChainContains(failure,
            cause -> cause instanceof SocketTimeoutException || cause instanceof TimeoutException);

    private static final Predicate<Throwable> isConnectionFailure = failure -> causeChainContains(failure,
            cause -> cause instanceof ConnectException || cause instanceof NoRouteToHostException
                    || cause instanceof UnknownHostException);

    private static final Predicate<Throwable> isClosedWhileReadingResponse = failure ->
            causeChainContains(failure, HttpClosedException.class::isInstance);

    /**
     * Classifies a call failure that delivered no exploitable HTTP status:
     * timeout -> BACKEND_TIMEOUT, connection closed mid-response ->
     * PROCESSING_ERROR, anything else -> GENERIC_ERROR. Cause always preserved.
     */
    public static Function<Throwable, BsiException> specifyCallFailure(String backendName) {
        return failure -> {
            if (isTimeout.test(failure) || isConnectionFailure.test(failure)) {
                return new BsiException(ErrorCode.BACKEND_TIMEOUT, failure, backendName);
            }
            if (isClosedWhileReadingResponse.test(failure)) {
                return new BsiException(ErrorCode.PROCESSING_ERROR, failure);
            }
            return new BsiException(ErrorCode.GENERIC_ERROR, failure, failure.getClass().getSimpleName());
        };
    }

    /** Invalid request (name missing or invalid) -> BAD_REQUEST. */
    public static final Supplier<BsiException> specifyInvalidRequest =
            () -> new BsiException(ErrorCode.BAD_REQUEST);

    /** Unreadable backend body -> PROCESSING_ERROR, cause preserved. */
    public static final Function<Throwable, BsiException> specifyReadingFailure =
            readingFailure -> new BsiException(ErrorCode.PROCESSING_ERROR, readingFailure);

    private static boolean causeChainContains(Throwable failure, Predicate<Throwable> matcher) {
        for (Throwable cause = failure; null != cause; cause = cause.getCause()) {
            if (matcher.test(cause)) {
                return true;
            }
        }
        return false;
    }
}
