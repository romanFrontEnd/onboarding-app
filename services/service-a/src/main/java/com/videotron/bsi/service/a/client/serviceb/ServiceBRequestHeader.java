package com.videotron.bsi.service.a.client.serviceb;

import static com.videotron.bsi.service.a.constants.Dictionary.CORRELATION_HEADERS;

import java.util.Optional;

import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;

/** service-b headers: X-* correlation propagation. No authentication (spec). */
@ApplicationScoped
@Slf4j
public class ServiceBRequestHeader extends ReactiveClientHeadersFactory {

    @Override
    public Uni<MultivaluedMap<String, String>> getHeaders(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        CORRELATION_HEADERS.forEach(name ->
                Optional.ofNullable(incomingHeaders.getFirst(name))
                        .ifPresent(value -> headers.add(name, value)));
        return Uni.createFrom().item(headers);
    }
}
