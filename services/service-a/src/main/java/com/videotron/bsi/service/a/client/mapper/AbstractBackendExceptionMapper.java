package com.videotron.bsi.service.a.client.mapper;

import static com.videotron.bsi.service.a.constants.Dictionary.LOG_BACKEND_ERROR_BODY;
import static com.videotron.bsi.service.a.constants.Dictionary.LOG_BACKEND_ERROR_STATUS;
import static com.videotron.bsi.service.a.constants.Dictionary.LOG_BACKEND_ERROR_UNREADABLE;
import static com.videotron.bsi.service.a.constants.Dictionary.X_TRANSACTION_ID;
import static com.videotron.bsi.service.a.exception.ErrorCode.fromHttpStatus;

import java.util.Optional;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.MDC;

import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ErrorCode;
import com.videotron.bsi.service.a.exception.ExternalServiceException;
import com.videotron.bsi.service.a.resources.ErrorRepresentation;

import io.opentelemetry.api.trace.Span;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class of every backend ResponseExceptionMapper.
 * RELAY: conformant ErrorRepresentation body carried untouched in an
 *        ExternalServiceException (remote status + body as answered).
 * NORMALIZE: every non-2xx mapped to the catalog via ErrorCode.fromHttpStatus.
 * Transport failures never reach this class — the adapter normalizes them.
 */
@Slf4j
public abstract class AbstractBackendExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    /** Backend name consigned in logs and MDC. */
    protected abstract String backendName();

    /** RELAY when the backend answers a conformant ErrorRepresentation, NORMALIZE otherwise. */
    protected abstract Strategy strategy();

    protected enum Strategy { RELAY, NORMALIZE }

    @Override
    public RuntimeException toThrowable(Response response) {
        int status = response.getStatus();
        ErrorRepresentation remoteBody = readRemoteBody(response);
        String correlationId = correlationId(response);
        logFailure(status, remoteBody, correlationId);
        if (status >= 500) {
            Span.current().setAttribute("error.backendStatus", status);
        }
        return switch (strategy()) {
            case RELAY     -> new ExternalServiceException(backendName(), status, remoteBody, correlationId);
            case NORMALIZE -> normalize(status, remoteBody);
        };
    }

    private ErrorRepresentation readRemoteBody(Response response) {
        if (!response.hasEntity()) {
            return null;
        }
        try {
            return response.readEntity(ErrorRepresentation.class);
        } catch (ProcessingException | IllegalStateException unreadable) {
            log.error(LOG_BACKEND_ERROR_UNREADABLE, backendName(), response.getStatus(), unreadable);
            return null;
        }
    }

    private static String correlationId(Response response) {
        return Optional.ofNullable(response.getHeaderString(X_TRANSACTION_ID))
                .orElseGet(() -> MDC.get(X_TRANSACTION_ID));
    }

    private void logFailure(int status, ErrorRepresentation body, String correlationId) {
        if (null != body) {
            log.error(LOG_BACKEND_ERROR_BODY, backendName(), status, body.getCode(), body.getReason(), correlationId);
        } else {
            log.error(LOG_BACKEND_ERROR_STATUS, backendName(), status, correlationId);
        }
    }

    /** Remote reason preserved verbatim when supplied; catalog default status. */
    protected BsiException normalize(int status, ErrorRepresentation remoteBody) {
        ErrorCode catalog = fromHttpStatus(status);
        return Optional.ofNullable(remoteBody)
                .map(ErrorRepresentation::getReason)
                .map(reason -> BsiException.relayed(catalog, reason,
                        catalog.defaultHttpStatus().getStatusCode()))
                .orElseGet(() -> toCatalogException(catalog, status));
    }

    protected BsiException toCatalogException(ErrorCode catalog, int status) {
        return catalog.expectedArgCount() == 0
                ? new BsiException(catalog)
                : new BsiException(catalog, backendName() + " answered HTTP " + status);
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }
}
