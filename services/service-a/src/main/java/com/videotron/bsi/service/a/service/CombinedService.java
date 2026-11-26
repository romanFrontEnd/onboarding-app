package com.videotron.bsi.service.a.service;

import static com.videotron.bsi.service.a.constants.Dictionary.SERVICE_A;
import static com.videotron.bsi.service.a.constants.Dictionary.X_TRANSACTION_ID;
import static com.videotron.bsi.service.a.exception.ErrorCode.GENERIC_ERROR;

// Java standard library
import java.util.UUID;

// Third-party libraries
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// Internal project packages
import com.videotron.bsi.service.a.businessrules.NameValidator;
import com.videotron.bsi.service.a.client.serviceb.ServiceBClientAdapter;
import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.CombinedException;
import com.videotron.bsi.service.a.mapping.CombinedMapper;
import com.videotron.bsi.service.a.model.HeaderParameter;
import com.videotron.bsi.service.a.resources.CombinedResponse;
import com.videotron.bsi.service.a.resources.CombinedResponseServiceA;
import com.videotron.bsi.service.a.resources.HelloResponse;
import com.videotron.bsi.service.a.utils.logging.LoggingUtils;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service class for Combined operations.
 */
@ApplicationScoped
@Slf4j
public class CombinedService {

    @Inject
    NameValidator nameValidator;

    @Inject
    ServiceBClientAdapter serviceBClientAdapter;

    @Inject
    CombinedMapper combinedMapper;

    @ConfigProperty(name = "info.app.version")
    String appVersion;

    /**
     * Builds the combined result of service-a and service-b.
     *
     * @param name the name to greet
     * @param headerParameter Header parameters
     * @return the combined response
     * @throws CombinedException If operation fails or is not implemented
     * @traced CombinedServiceTest#getCombined_validName_should_returnBothBlocks,
     *         CombinedServiceTest#getCombined_invalidName_should_failWithoutCallingServiceB,
     *         CombinedServiceTest#getCombined_serviceBFailure_should_propagateServiceBError
     * @ref doc/spec/B2-5095/spec.md, Validation Process, Mapping and Transformation Rules
     */
    @WithSpan("getCombined")
    public CombinedResponse getCombined(String name, HeaderParameter headerParameter) throws CombinedException {
        // Ref: spec Mapping rules — X-Transaction-Id passed through, generated if absent
        String transactionId = ensureTransactionId(headerParameter);
        log.info("getCombined - entry xTransactionId=%s".formatted(transactionId));

        try {
            // Traced: CombinedServiceTest#getCombined_invalidName_should_failWithoutCallingServiceB
            // Ref: spec Validation Process step 1
            nameValidator.validate(name);

            // Traced: CombinedServiceTest#getCombined_validName_should_returnBothBlocks
            // Ref: spec Validation Process steps 2-4 — call service-b with timeout and retries
            HelloResponse hello = serviceBClientAdapter.getHello(name);

            // Ref: spec Mapping rules — service-a block (service, version) next to the service-b block
            CombinedResponse result = CombinedResponse.builder()
                .name(name)
                .serviceA(CombinedResponseServiceA.builder().service(SERVICE_A).version(appVersion).build())
                .serviceB(combinedMapper.toServiceB(hello))
                .build();

            log.info("getCombined - exit");
            return result;

        } catch (BsiException e) {
            log.error("getCombined - error", e);
            throw e;
        } catch (Exception e) {
            log.error("getCombined - error", e);
            throw new BsiException(GENERIC_ERROR, e, e.getClass().getSimpleName());
        }
    }

    private String ensureTransactionId(HeaderParameter headerParameter) {
        if (StringUtils.isBlank(headerParameter.getXTransactionId())) {
            headerParameter.setXTransactionId(UUID.randomUUID().toString());
        }
        LoggingUtils.setLoggingContextField(X_TRANSACTION_ID, headerParameter.getXTransactionId());
        return headerParameter.getXTransactionId();
    }
}
