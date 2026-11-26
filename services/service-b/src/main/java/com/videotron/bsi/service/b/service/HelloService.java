package com.videotron.bsi.service.b.service;

import static com.videotron.bsi.service.b.constants.Dictionary.GREETING;
import static com.videotron.bsi.service.b.constants.Dictionary.SERVICE_NAME;

// Third-party libraries
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// Internal project packages
import com.videotron.bsi.service.b.exception.HelloException;
import com.videotron.bsi.service.b.model.HeaderParameter;
import com.videotron.bsi.service.b.resources.HelloResponse;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service class for Hello operations.
 */
@ApplicationScoped
@Slf4j
public class HelloService {

    @ConfigProperty(name = "info.app.version")
    String appVersion;

    /**
     * Builds the greeting for a name.
     *
     * @param name the name to greet
     * @param headerParameter Header parameters
     * @return the greeting (message, service, version)
     * @throws HelloException If operation fails
     * @traced HelloServiceTest#getHello_validName_should_returnGreetingFromServiceB
     * @ref doc/spec/B2-5095/spec.md, API contract (B)
     */
    @WithSpan("getHello")
    public HelloResponse getHello(String name, HeaderParameter headerParameter) throws HelloException {
        log.info("Starting getHello operation");

        // Traced: HelloServiceTest#getHello_validName_should_returnGreetingFromServiceB
        // Ref: spec API contract (B) — {"message":"Hello, <name>","service":"service-b","version":"1.0.0"}
        return HelloResponse.builder()
            .message(GREETING.formatted(name))
            .service(SERVICE_NAME)
            .version(appVersion)
            .build();
    }
}
