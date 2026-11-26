package com.videotron.bsi.service.a.configuration.observability;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@EnableObserved
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ObservedInterceptor {

    private final ObservationRegistry registry;

    @Inject
    public ObservedInterceptor(ObservationRegistry registry) {
        this.registry = registry;
    }

    @AroundInvoke
    public Object observe(InvocationContext context) throws Exception {
        var annotation = Optional.ofNullable(context.getMethod().getAnnotation(Observed.class))
                .or(() -> Optional.ofNullable(context.getTarget().getClass().getAnnotation(Observed.class)));
        if (annotation.isEmpty()) {
            return context.proceed();
        }
        var returnType = context.getMethod().getReturnType();
        if (CompletionStage.class.isAssignableFrom(returnType)
                || Future.class.isAssignableFrom(returnType)
                || Flow.Publisher.class.isAssignableFrom(returnType)
                || Uni.class.isAssignableFrom(returnType)
                || Multi.class.isAssignableFrom(returnType)) {
            throw new IllegalStateException("The local Observed interceptor supports synchronous methods only");
        }
        var observed = annotation.orElseThrow();
        var tags = observed.lowCardinalityKeyValues();
        if (tags.length % 2 != 0 || IntStream.range(0, tags.length / 2)
                .anyMatch(index -> tags[index * 2].isBlank())) {
            throw new IllegalArgumentException("Observed tags require key/value pairs with non-blank keys");
        }
        var method = context.getMethod();
        var name = observed.name().isBlank() ? "method.observed" : observed.name();
        var contextualName = observed.contextualName().isBlank()
                ? method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                : observed.contextualName();
        var observation = Observation.createNotStarted(name, registry)
                .contextualName(contextualName)
                .lowCardinalityKeyValue("class", method.getDeclaringClass().getName())
                .lowCardinalityKeyValue("method", method.getName())
                .lowCardinalityKeyValues(KeyValues.of(tags));
        observation.start();
        try {
            try (var scope = observation.openScope()) {
                return context.proceed();
            }
        } catch (Exception | Error error) {
            observation.error(error);
            throw error;
        } finally {
            observation.stop();
        }
    }
}
