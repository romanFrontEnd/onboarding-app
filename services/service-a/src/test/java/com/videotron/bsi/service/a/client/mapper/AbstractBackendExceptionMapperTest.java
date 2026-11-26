package com.videotron.bsi.service.a.client.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import com.videotron.bsi.service.a.client.serviceb.ServiceBExceptionMapper;
import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ExternalServiceException;
import com.videotron.bsi.service.a.resources.ErrorRepresentation;

/**
 * Coverage-closure tests of the backend exception mapping (verification pass, gap G4): NORMALIZE as used by
 * service-a, plus the RELAY strategy and the unreadable-body branch.
 *
 * @ref doc/spec/B2-5095/spec.md, Error Management (service-b 4xx and 5xx answers map to 502 SERVICE_B_ERROR)
 */
class AbstractBackendExceptionMapperTest {

    private final ServiceBExceptionMapper normalizing = new ServiceBExceptionMapper();

    /** RELAY variant, to exercise the relay path of the abstract mapper. */
    private static final class RelayMapper extends AbstractBackendExceptionMapper {
        @Override protected String backendName() { return "service-b"; }
        @Override protected Strategy strategy() { return Strategy.RELAY; }
    }

    private static Response response(int status, ErrorRepresentation body) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        when(response.hasEntity()).thenReturn(null != body);
        when(response.readEntity(ErrorRepresentation.class)).thenReturn(body);
        when(response.getHeaderString("X-Transaction-Id")).thenReturn("tx-1");
        return response;
    }

    @Test
    void toThrowable_serverError_should_normalizeToServiceBError() {
        RuntimeException result = normalizing.toThrowable(response(500, null));

        BsiException bsi = assertInstanceOf(BsiException.class, result);
        assertEquals("SERVICE_B_ERROR", bsi.getCode());
        assertEquals(502, bsi.getHttpStatus());
    }

    @Test
    void toThrowable_clientErrorWithoutBody_should_normalizeToServiceBError() {
        BsiException bsi = assertInstanceOf(BsiException.class, normalizing.toThrowable(response(404, null)));

        assertEquals("SERVICE_B_ERROR", bsi.getCode());
        assertEquals(502, bsi.getHttpStatus());
    }

    @Test
    void toThrowable_clientErrorWithBody_should_keepRemoteReasonWithCatalogStatus() {
        ErrorRepresentation body = ErrorRepresentation.builder()
            .code("INVALID_REQUEST").reason("remote reason").atType("ErrorRepresentation").build();

        BsiException bsi = assertInstanceOf(BsiException.class, normalizing.toThrowable(response(400, body)));

        assertEquals("SERVICE_B_ERROR", bsi.getCode());
        assertEquals("remote reason", bsi.getReason());
        assertEquals(502, bsi.getHttpStatus());
    }

    @Test
    void toThrowable_unreadableBody_should_behaveLikeNoBody() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);
        when(response.hasEntity()).thenReturn(true);
        when(response.readEntity(ErrorRepresentation.class)).thenThrow(new ProcessingException("unreadable"));

        BsiException bsi = assertInstanceOf(BsiException.class, normalizing.toThrowable(response));

        assertEquals("SERVICE_B_ERROR", bsi.getCode());
    }

    @Test
    void toThrowable_relayStrategy_should_carryRemoteStatusAndBodyUntouched() {
        ErrorRepresentation body = ErrorRepresentation.builder()
            .code("X").reason("remote").atType("ErrorRepresentation").build();

        ExternalServiceException result = assertInstanceOf(ExternalServiceException.class,
            new RelayMapper().toThrowable(response(503, body)));

        assertEquals("service-b", result.getRemoteService());
        assertEquals(503, result.getRemoteStatus());
        assertSame(body, result.getRemoteBody());
        assertEquals("tx-1", result.getCorrelationId());
    }

    @Test
    void handles_should_acceptOnlyErrorStatuses() {
        assertFalse(normalizing.handles(200, new MultivaluedHashMap<>()));
        assertTrue(normalizing.handles(400, new MultivaluedHashMap<>()));
        assertTrue(normalizing.handles(503, new MultivaluedHashMap<>()));
    }
}
