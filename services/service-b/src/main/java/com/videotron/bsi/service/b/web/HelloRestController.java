package com.videotron.bsi.service.b.web;

import static com.videotron.bsi.service.b.constants.Dictionary.HELLO_COUNTED;
import static com.videotron.bsi.service.b.constants.Dictionary.HELLO_OBSERVED;

// Third-party libraries
import io.micrometer.core.annotation.Counted;
import io.micrometer.observation.annotation.Observed;
import io.smallrye.common.annotation.RunOnVirtualThread;
import org.jboss.resteasy.reactive.RestResponse;
import lombok.extern.slf4j.Slf4j;

// Internal project packages
import com.videotron.bsi.service.b.configuration.observability.EnableObserved;
import com.videotron.bsi.service.b.model.HeaderParameter;
import com.videotron.bsi.service.b.resources.HelloResponse;
import com.videotron.bsi.service.b.service.HelloService;

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
 * REST controller for Hello operations.
 */
@ApplicationScoped
@EnableObserved
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class HelloRestController {

    @Inject
    HelloService helloService;

    /**
     * Greeting for a name.
     *
     * @param name the name to greet (required)
     * @return the greeting with status 200
     */
    @GET
    @RunOnVirtualThread
    @Observed(name = HELLO_OBSERVED)
    @Counted(value = HELLO_COUNTED)
    @Path("/hello")
    public RestResponse<HelloResponse> getHello(
        @Valid
        @BeanParam
        HeaderParameter headerParameter,
        @QueryParam("name") String name) {

        log.info("Start getHello for xTransactionId:%s".formatted(headerParameter.getXTransactionId()));

        HelloResponse response = helloService.getHello(name, headerParameter);

        return RestResponse.ResponseBuilder.<HelloResponse>create(Status.OK.getStatusCode())
                .entity(response)
                .replaceAll(headerParameter.getResponseHeaders())
                .build();
    }
}
