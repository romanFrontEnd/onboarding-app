package com.videotron.bsi.service.a.web;

import static com.videotron.bsi.service.a.constants.Dictionary.COMBINED_COUNTED;
import static com.videotron.bsi.service.a.constants.Dictionary.COMBINED_OBSERVED;

// Third-party libraries
import io.micrometer.core.annotation.Counted;
import io.micrometer.observation.annotation.Observed;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.jboss.resteasy.reactive.RestResponse;
import lombok.extern.slf4j.Slf4j;

// Internal project packages
import com.videotron.bsi.service.a.configuration.observability.EnableObserved;
import com.videotron.bsi.service.a.model.HeaderParameter;
import com.videotron.bsi.service.a.resources.CombinedResponse;
import com.videotron.bsi.service.a.service.CombinedService;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST controller for Combined operations.
 */
@ApplicationScoped
@EnableObserved
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class CombinedRestController {

    @Inject
    CombinedService combinedService;

    /**
     * Combined result of service-a and service-b.
     *
     * @param name the name to greet (required)
     * @return the combined result with status 200
     */
    @GET
    @RunOnVirtualThread
    @Observed(name = COMBINED_OBSERVED)
    @Counted(value = COMBINED_COUNTED)
    @Path("/combined")
    public RestResponse<CombinedResponse> getCombined(
        @Valid
        @BeanParam
        HeaderParameter headerParameter,
        @QueryParam("name") String name) {

        log.info("Start getCombined for xTransactionId:%s".formatted(headerParameter.getXTransactionId()));

        CombinedResponse response = combinedService.getCombined(name, headerParameter);

        return RestResponse.ResponseBuilder.<CombinedResponse>create(Status.OK.getStatusCode())
                .entity(response)
                .replaceAll(headerParameter.getResponseHeaders())
                .build();
    }
}
