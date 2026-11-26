package com.videotron.bsi.service.a.exception;

import com.videotron.bsi.service.a.resources.ErrorRepresentation;

/** Carries a backend error answer (status >= 400) untouched: remote status and
 *  remote ErrorRepresentation relayed as-is. NOT a catalog error — every
 *  transform guard excludes it; never re-normalize mid-chain. */
public class ExternalServiceException extends RuntimeException {

    private final String remoteService;
    private final int remoteStatus;
    private final transient ErrorRepresentation remoteBody;
    private final String correlationId;

    public ExternalServiceException(String remoteService, int remoteStatus,
            ErrorRepresentation remoteBody, String correlationId) {
        super(remoteService + " answered HTTP " + remoteStatus);
        this.remoteService = remoteService;
        this.remoteStatus = remoteStatus;
        this.remoteBody = remoteBody;
        this.correlationId = correlationId;
    }

    public String getRemoteService() { return remoteService; }
    public int getRemoteStatus() { return remoteStatus; }
    public ErrorRepresentation getRemoteBody() { return remoteBody; }
    public String getCorrelationId() { return correlationId; }
}
