package com.videotron.bsi.service.a.exception;

import static com.videotron.bsi.service.a.constants.Dictionary.ERROR_TYPE;
import static com.videotron.bsi.service.a.constants.Dictionary.LOG_MAPPER_ERROR;

import java.util.Optional;

import com.videotron.bsi.service.a.resources.ErrorRepresentation;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/** Terminal translation of every domain exception into the BSI error response. */
@Slf4j
public class BsiExceptionMapper {

    @ServerExceptionMapper
    public Response toResponse(BsiException exception) {
        log.error(LOG_MAPPER_ERROR, exception);
        return Response.status(exception.getHttpStatus())
                .entity(ErrorRepresentation.builder()
                        .atType(ERROR_TYPE)
                        .code(exception.getCode())
                        .reason(exception.getReason())
                        .build())
                .build();
    }

    /** Relays the remote answer as-is; synthesizes a minimal body when absent. */
    @ServerExceptionMapper
    public Response toResponse(ExternalServiceException exception) {
        ErrorRepresentation body = Optional.ofNullable(exception.getRemoteBody())
                .orElseGet(() -> ErrorRepresentation.builder()
                        .atType(ERROR_TYPE)
                        .code(exception.getRemoteService() + "-" + exception.getRemoteStatus())
                        .reason(exception.getMessage())
                        .build());
        return Response.status(exception.getRemoteStatus()).entity(body).build();
    }
}
