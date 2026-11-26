package com.videotron.bsi.service.a.client.serviceb;

import static com.videotron.bsi.service.a.constants.Dictionary.LOG_BACKEND_CALL_FAILED;
import static com.videotron.bsi.service.a.constants.Dictionary.SERVICE_B;
import static com.videotron.bsi.service.a.function.BsiExceptionFunction.specifyCallFailure;

import java.util.function.Function;

import org.eclipse.microprofile.rest.client.inject.RestClient;

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
 */
@ApplicationScoped
@Slf4j
public class ServiceBClientAdapter {

    @Inject
    @RestClient
    ServiceBClient client;

    public HelloResponse getHello(String name) {
        return call(name).getOrElseThrow(Function.identity());
    }

    private Either<RuntimeException, HelloResponse> call(String name) {
        return Try.of(() -> client.getHello(name))
                .toEither()
                .mapLeft(this::toDomainFailure);
    }

    private RuntimeException toDomainFailure(Throwable failure) {
        if (failure instanceof BsiException || failure instanceof ExternalServiceException) {
            return (RuntimeException) failure;
        }
        log.error(LOG_BACKEND_CALL_FAILED, SERVICE_B, failure);
        return specifyCallFailure(SERVICE_B).apply(failure);
    }
}
