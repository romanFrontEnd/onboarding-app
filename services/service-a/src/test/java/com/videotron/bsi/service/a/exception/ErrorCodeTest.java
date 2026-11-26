package com.videotron.bsi.service.a.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.core.Response.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Coverage-closure tests of {@link ErrorCode} (verification pass, gap G2).
 *
 * @ref doc/spec/B2-5095/spec.md, Error Management (spec v0.8: service-b 4xx and 5xx map to 502 SERVICE_B_ERROR)
 */
@QuarkusTest
class ErrorCodeTest {

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 409, 429, 500, 502, 503, 504})
    void fromHttpStatus_serviceBErrorStatus_should_mapToBackendError(int status) {
        ErrorCode code = ErrorCode.fromHttpStatus(status);

        assertEquals(ErrorCode.BACKEND_ERROR, code);
        assertEquals("SERVICE_B_ERROR", code.code());
        assertEquals(Status.BAD_GATEWAY, code.defaultHttpStatus());
    }

    @Test
    void fromHttpStatus_unmappedStatus_should_fallBackToGeneric() {
        assertEquals(ErrorCode.GENERIC_ERROR, ErrorCode.fromHttpStatus(418));
    }

    @Test
    void backendTimeout_should_carryServiceBUnavailable() {
        assertEquals("SERVICE_B_UNAVAILABLE", ErrorCode.BACKEND_TIMEOUT.code());
        assertEquals(Status.SERVICE_UNAVAILABLE, ErrorCode.BACKEND_TIMEOUT.defaultHttpStatus());
        assertEquals("service-b did not respond in time", ErrorCode.BACKEND_TIMEOUT.reason("service-b"));
    }

    @Test
    void reason_withWrongArgumentCount_should_throwIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> ErrorCode.BACKEND_TIMEOUT.reason());
        assertThrows(IllegalArgumentException.class, () -> ErrorCode.BACKEND_ERROR.reason("unexpected"));
    }

    @Test
    void fromCode_aliasedCode_should_returnFirstDeclaredConstant() {
        assertEquals(ErrorCode.BACKEND_ERROR, ErrorCode.fromCode("SERVICE_B_ERROR").orElseThrow());
        assertEquals(ErrorCode.BACKEND_TIMEOUT, ErrorCode.fromCode("SERVICE_B_UNAVAILABLE").orElseThrow());
        assertTrue(ErrorCode.fromCode("UNKNOWN").isEmpty());
    }
}
