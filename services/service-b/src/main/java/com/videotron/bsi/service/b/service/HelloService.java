package com.videotron.bsi.service.b.service;

// Third-party libraries
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

// Internal project packages
import com.videotron.bsi.service.b.exception.HelloException;
import com.videotron.bsi.service.b.model.HeaderParameter;
import com.videotron.bsi.service.b.resources.HelloResponse;
import static com.videotron.bsi.service.b.exception.ErrorCode.NOT_IMPLEMENTED;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

/**
 * Service class for Hello operations.
 */
@ApplicationScoped
@Slf4j
public class HelloService {

    /**
     * Builds the greeting for a name.
     *
     * @param name the name to greet
     * @param headerParameter Header parameters
     * @return the greeting (message, service, version)
     * @throws HelloException If operation fails or is not implemented
     */
    @WithSpan("getHello")
    public HelloResponse getHello(String name, HeaderParameter headerParameter) throws HelloException {
        log.info("Starting getHello operation");

        throw new HelloException(
            NOT_IMPLEMENTED.code(),
            NOT_IMPLEMENTED.reason("getHello"),
            Status.NOT_IMPLEMENTED.getStatusCode()
        );
    }
}
