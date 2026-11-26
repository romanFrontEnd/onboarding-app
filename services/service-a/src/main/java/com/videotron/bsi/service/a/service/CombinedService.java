package com.videotron.bsi.service.a.service;

// Third-party libraries
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

// Internal project packages
import com.videotron.bsi.service.a.exception.CombinedException;
import com.videotron.bsi.service.a.model.HeaderParameter;
import com.videotron.bsi.service.a.resources.CombinedResponse;
import static com.videotron.bsi.service.a.exception.ErrorCode.NOT_IMPLEMENTED;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

/**
 * Service class for Combined operations.
 */
@ApplicationScoped
@Slf4j
public class CombinedService {

    /**
     * Builds the combined result of service-a and service-b.
     *
     * @param name the name to greet
     * @param headerParameter Header parameters
     * @return the combined response
     * @throws CombinedException If operation fails or is not implemented
     */
    @WithSpan("getCombined")
    public CombinedResponse getCombined(String name, HeaderParameter headerParameter) throws CombinedException {
        log.info("Starting getCombined operation");

        throw new CombinedException(
            NOT_IMPLEMENTED.code(),
            NOT_IMPLEMENTED.reason("getCombined"),
            Status.NOT_IMPLEMENTED.getStatusCode()
        );
    }
}
