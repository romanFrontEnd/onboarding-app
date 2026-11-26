package com.videotron.bsi.service.b.exception;

import static com.videotron.bsi.service.b.constants.Dictionary.ERROR_TYPE;

import com.videotron.bsi.service.b.resources.ErrorRepresentation;

import java.util.Objects;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class HelloExceptionHandler implements ExceptionMapper<HelloException> {

    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    @Inject
    Tracer configuredTracer;

    public HelloExceptionHandler() {
        this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    @Override
    public Response toResponse(HelloException exception) {
        log.error("{}: {} - {}", exception.getClass().getSimpleName(), exception.getCode(), exception.getReason());
        tagError(exception.getCode(), exception.getReason());

        ErrorRepresentation errorRepresentation = ErrorRepresentation.builder()
                               .atType(ERROR_TYPE)
                               .code(exception.getCode())
                               .reason(exception.getReason())
                               .build();
        
        return Response.status(exception.getStatus()).replaceAll(headers).entity(errorRepresentation).build();
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
