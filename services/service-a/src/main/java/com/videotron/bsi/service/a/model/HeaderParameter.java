package com.videotron.bsi.service.a.model;

import static com.videotron.bsi.service.a.constants.Dictionary.X_TRANSACTION_ID;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestHeader;

/**
 * Container for HTTP headers received and echoed by the API.
 * Used with {@code @BeanParam} in REST controllers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderParameter {

    @Parameter(name = X_TRANSACTION_ID, description = "Transaction ID", in = ParameterIn.HEADER)
    @RestHeader(value = X_TRANSACTION_ID)
    private String xTransactionId;

    /**
     * Builds the response headers map, echoing the input headers.
     *
     * @return multivalued map containing the same headers received on the request
     */
    public MultivaluedMap<String, Object> getResponseHeaders() {
        MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
        responseHeaders.add(X_TRANSACTION_ID, xTransactionId);
        return responseHeaders;
    }
}
