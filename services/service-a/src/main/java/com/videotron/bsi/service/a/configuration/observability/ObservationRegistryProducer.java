package com.videotron.bsi.service.a.configuration.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.quarkus.arc.DefaultBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
public class ObservationRegistryProducer {

    @Produces
    @Singleton
    @DefaultBean
    public ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
        var registry = ObservationRegistry.create();
        registry.observationConfig()
                .observationHandler(new DefaultMeterObservationHandler(meterRegistry));
        return registry;
    }
}
