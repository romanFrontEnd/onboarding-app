package com.videotron.bsi.service.b.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

/**
 * Coverage-closure tests of {@link ErrorCode} (verification pass, gap G1).
 *
 * @ref doc/spec/B2-5095/spec.md, Error Management
 */
class ErrorCodeTest {

    @Test
    void reason_withMatchingArguments_should_formatTemplate() {
        assertEquals("Method not implemented: getHello", ErrorCode.NOT_IMPLEMENTED.reason("getHello"));
        assertEquals("Invalid request: name is missing or invalid", ErrorCode.BAD_REQUEST.reason());
    }

    @Test
    void reason_withWrongArgumentCount_should_throwIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> ErrorCode.NOT_IMPLEMENTED.reason());
        assertThrows(IllegalArgumentException.class, () -> ErrorCode.BAD_REQUEST.reason("unexpected"));
    }

    @Test
    void catalogue_should_carryTheSpecCodeAndStatus() {
        assertEquals("INVALID_REQUEST", ErrorCode.BAD_REQUEST.code());
        assertEquals(Status.BAD_REQUEST, ErrorCode.BAD_REQUEST.defaultHttpStatus());
        assertEquals(ErrorFamily.VALIDATION, ErrorCode.BAD_REQUEST.family());
        assertEquals("39101", ErrorCode.NOT_IMPLEMENTED.code());
        assertEquals("99999", ErrorCode.GENERIC_ERROR.code());
    }

    @Test
    void fromHttpStatus_should_mapKnownAndFallBackToGeneric() {
        assertEquals(ErrorCode.BAD_REQUEST, ErrorCode.fromHttpStatus(400));
        assertEquals(ErrorCode.GENERIC_ERROR, ErrorCode.fromHttpStatus(418));
    }

    @Test
    void fromCode_should_findKnownCodesOnly() {
        assertEquals(ErrorCode.BAD_REQUEST, ErrorCode.fromCode("INVALID_REQUEST").orElseThrow());
        assertTrue(ErrorCode.fromCode("UNKNOWN").isEmpty());
    }
}
