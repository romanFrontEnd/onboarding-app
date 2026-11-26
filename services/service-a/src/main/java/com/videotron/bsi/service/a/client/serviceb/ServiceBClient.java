package com.videotron.bsi.service.a.client.serviceb;

import static com.videotron.bsi.service.a.constants.Dictionary.X_TRANSACTION_ID;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.videotron.bsi.service.a.resources.HelloResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * service-b backend client. Registered under config key "service-b" —
 * quarkus.rest-client.service-b.url/connect-timeout/read-timeout supply the
 * per-profile base URL and timeouts. Errors: ServiceBExceptionMapper (pattern 34);
 * transport failures: ServiceBClientAdapter (pattern 36).
 */
@Path("/api/v1")
@RegisterRestClient(configKey = "service-b")
@RegisterProvider(ServiceBExceptionMapper.class)
@RegisterClientHeaders(ServiceBRequestHeader.class)
public interface ServiceBClient {

    // Imperative flavor — the adapter uses the Try/Either variant.
    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    HelloResponse getHello(@QueryParam("name") String name, @HeaderParam(X_TRANSACTION_ID) String transactionId);
}
