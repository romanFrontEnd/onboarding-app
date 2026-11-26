package com.videotron.bsi.service.a.client.serviceb;

import static com.videotron.bsi.service.a.constants.Dictionary.LOG_BACKEND_CALL_FAILED;
import static com.videotron.bsi.service.a.constants.Dictionary.SERVICE_B;
import static com.videotron.bsi.service.a.constants.Dictionary.X_TRANSACTION_ID;
import static com.videotron.bsi.service.a.exception.ErrorCode.BACKEND_TIMEOUT;
import static com.videotron.bsi.service.a.function.BsiExceptionFunction.specifyCallFailure;

import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.MDC;

import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ExternalServiceException;
import com.videotron.bsi.service.a.resources.HelloResponse;

import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * service-b client adapter — the ONLY owner of transport-failure
 * normalization. HTTP errors arrive already classified by ServiceBExceptionMapper
 * and pass the guard untouched; everything else goes through specifyCallFailure.
 * Retries timeouts and connection errors only (spec v0.7), never a 4xx or 5xx answer.
 */
@ApplicationScoped
@Slf4j
public class ServiceBClientAdapter {

    @Inject
    @RestClient
    ServiceBClient client;

    @ConfigProperty(name = "service-b.retries")
    int retries;

    @ConfigProperty(name = "service-b.retry-backoff")
    long retryBackoffMillis;

    /**
     * @traced CombinedSteps (SC-001, SC-003, SC-004, SC-005, SC-006, SC-016, SC-017, SC-020)
     * @ref doc/spec/B2-5095/spec.md, Validation Process steps 2-4 and Error Management
     */
    public HelloResponse getHello(String name) {
        // Ref: spec Mapping rules — the transaction ID of the request is passed to service-b
        String transactionId = MDC.get(X_TRANSACTION_ID);
        for (int attempt = 1; ; attempt++) {
            Either<RuntimeException, HelloResponse> outcome = call(name, transactionId);
            if (outcome.isRight()) {
                return outcome.get();
            }
            RuntimeException failure = outcome.getLeft();
            // Traced: SC-005, SC-020 — no retry on a service-b answer; SC-006 — retries are bounded
            if (attempt > retries || !isRetryable(failure)) {
                throw failure;
            }
            // Ref: spec Validation Process step 3 — fixed backoff between attempts
            try {
                Thread.sleep(retryBackoffMillis);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw failure;
            }
        }
    }

    private Either<RuntimeException, HelloResponse> call(String name, String transactionId) {
        return Try.of(() -> client.getHello(name, transactionId))
                .toEither()
                .mapLeft(this::toDomainFailure);
    }

    private boolean isRetryable(RuntimeException failure) {
        return failure instanceof BsiException bsi && bsi.getErrorCode() == BACKEND_TIMEOUT;
    }

    private RuntimeException toDomainFailure(Throwable failure) {
        if (failure instanceof BsiException || failure instanceof ExternalServiceException) {
            return (RuntimeException) failure;
        }
        log.error(LOG_BACKEND_CALL_FAILED, SERVICE_B, failure);
        return specifyCallFailure(SERVICE_B).apply(failure);
    }
}
