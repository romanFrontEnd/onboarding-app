package com.videotron.bsi.service.b.exception;

import static com.videotron.bsi.service.b.constants.Dictionary.ERROR_TYPE;
import static com.videotron.bsi.service.b.constants.Dictionary.LOG_BEAN_VALIDATION_VIOLATION;
import static com.videotron.bsi.service.b.exception.ErrorCode.BAD_REQUEST;

import java.util.stream.Collectors;

import com.videotron.bsi.service.b.resources.ErrorRepresentation;

import io.opentelemetry.api.trace.Span;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates Bean Validation violations (e.g. {@code @Valid @BeanParam HeaderParameter}) into an
 * ErrorRepresentation 400 / BAD_REQUEST. The violation detail is logged and tagged on the span,
 * never exposed in the response body. A violation on a return value is a server bug: the exception
 * is rethrown (like the native Quarkus mapper) and ends up as a 500.
 */
@Provider
@Slf4j
public class ResteasyReactiveViolationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    public ResteasyReactiveViolationExceptionMapper() {
        this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        if (hasReturnValueViolation(exception)) {
            throw exception;
        }

        String detail = exception.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .sorted()
                .collect(Collectors.joining("; "));

        log.warn(LOG_BEAN_VALIDATION_VIOLATION, detail);
        Span span = Span.current();
        span.setAttribute("errorCode", BAD_REQUEST.code());
        span.setAttribute("errorReason", detail);

        ErrorRepresentation restFault = ErrorRepresentation.builder()
                .code(BAD_REQUEST.code())
                .reason(BAD_REQUEST.reason())
                .atType(ERROR_TYPE)
                .build();

        return Response.status(BAD_REQUEST.defaultHttpStatus()).replaceAll(headers).entity(restFault).build();
    }

    private boolean hasReturnValueViolation(ResteasyReactiveViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getPropertyPath)
                .anyMatch(this::isReturnValue);
    }

    private boolean isReturnValue(Path path) {
        var nodes = path.iterator();
        return nodes.hasNext()
                && nodes.next().getKind() == ElementKind.METHOD
                && nodes.hasNext()
                && nodes.next().getKind() == ElementKind.RETURN_VALUE;
    }
}
