package com.videotron.bsi.service.a.exception;

import static com.videotron.bsi.service.a.constants.Dictionary.ERROR_TYPE;

import com.videotron.bsi.service.a.resources.ErrorRepresentation;
import static com.videotron.bsi.service.a.exception.ErrorCode.GENERIC_ERROR;

import java.util.Objects;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GenericExceptionHandler implements ExceptionMapper<Exception> {

    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    @Inject
    Tracer configuredTracer;

    public GenericExceptionHandler() {
        this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    @Override
    public Response toResponse(Exception exception) {
        log.error("Unhandled exception occurred", exception);
        tagError(GENERIC_ERROR.code(), GENERIC_ERROR.reason(exception.getMessage()));

        ErrorRepresentation errorRepresentation = ErrorRepresentation.builder()
                               .atType(ERROR_TYPE)
                               .code(GENERIC_ERROR.code())
                               .reason(exception.getMessage())
                               .build();
        
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .replaceAll(headers)
                .entity(errorRepresentation)
                .build();
    }
    
    private void tagError(String code, String reason) {
        Span span = Span.current();
        if (Objects.isNull(span)) {
            span = configuredTracer.spanBuilder("tagError").startSpan();
        }
        span.setAttribute("errorCode", code);
        span.setAttribute("errorReason", reason);
    }
}
