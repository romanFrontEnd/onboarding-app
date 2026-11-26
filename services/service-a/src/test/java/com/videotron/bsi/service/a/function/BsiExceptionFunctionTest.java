package com.videotron.bsi.service.a.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ErrorCode;

import io.vertx.core.http.HttpClosedException;

/**
 * Coverage-closure tests of {@link BsiExceptionFunction} (verification pass, gap G3).
 *
 * @ref doc/spec/B2-5095/spec.md, Error Management and spec v0.8 (refused connection -> SERVICE_B_UNAVAILABLE)
 */
class BsiExceptionFunctionTest {

    private static final String BACKEND = "service-b";

    @Test
    void specifyCallFailure_timeout_should_beServiceBUnavailable() {
        BsiException result = BsiExceptionFunction.specifyCallFailure(BACKEND)
            .apply(new RuntimeException("wrapper", new TimeoutException("slow")));

        assertEquals(ErrorCode.BACKEND_TIMEOUT, result.getErrorCode());
        assertEquals("SERVICE_B_UNAVAILABLE", result.getCode());
        assertEquals(503, result.getHttpStatus());
    }

    @Test
    void specifyCallFailure_socketTimeout_should_beServiceBUnavailable() {
        BsiException result = BsiExceptionFunction.specifyCallFailure(BACKEND)
            .apply(new SocketTimeoutException("read timed out"));

        assertEquals(ErrorCode.BACKEND_TIMEOUT, result.getErrorCode());
    }

    @Test
    void specifyCallFailure_refusedOrUnreachableConnection_should_beServiceBUnavailable() {
        for (Throwable failure : new Throwable[] {
                new ConnectException("refused"), new NoRouteToHostException("no route"),
                new UnknownHostException("unknown host") }) {
            BsiException result = BsiExceptionFunction.specifyCallFailure(BACKEND)
                .apply(new RuntimeException("wrapper", failure));

            assertEquals("SERVICE_B_UNAVAILABLE", result.getCode());
            assertEquals(503, result.getHttpStatus());
        }
    }

    @Test
    void specifyCallFailure_connectionClosedMidResponse_should_beServiceBError() {
        BsiException result = BsiExceptionFunction.specifyCallFailure(BACKEND)
            .apply(new RuntimeException("wrapper", new HttpClosedException("closed")));

        assertEquals(ErrorCode.PROCESSING_ERROR, result.getErrorCode());
        assertEquals("SERVICE_B_ERROR", result.getCode());
        assertEquals(502, result.getHttpStatus());
    }

    @Test
    void specifyCallFailure_otherFailure_should_beGenericErrorWithCause() {
        IllegalStateException cause = new IllegalStateException("boom");

        BsiException result = BsiExceptionFunction.specifyCallFailure(BACKEND).apply(cause);

        assertEquals(ErrorCode.GENERIC_ERROR, result.getErrorCode());
        assertEquals("99999", result.getCode());
        assertSame(cause, result.getCause());
    }

    @Test
    void specifyReadingFailure_should_beServiceBErrorWithCause() {
        RuntimeException cause = new RuntimeException("unreadable");

        BsiException result = BsiExceptionFunction.specifyReadingFailure.apply(cause);

        assertEquals(ErrorCode.PROCESSING_ERROR, result.getErrorCode());
        assertSame(cause, result.getCause());
    }

    @Test
    void specifyInvalidRequest_should_beInvalidRequest400() {
        BsiException result = BsiExceptionFunction.specifyInvalidRequest.get();

        assertEquals("INVALID_REQUEST", result.getCode());
        assertEquals(400, result.getHttpStatus());
    }
}
