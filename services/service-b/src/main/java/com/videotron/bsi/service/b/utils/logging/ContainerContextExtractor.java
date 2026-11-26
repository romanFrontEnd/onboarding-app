package com.videotron.bsi.service.b.utils.logging;

// Java standard library
import java.io.IOException;
import java.util.UUID;

// Third-party libraries
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.MDC;

// Internal project packages
import com.videotron.bsi.service.b.configuration.BsiLogProperties;
import com.videotron.bsi.service.b.constants.logging.LoggingConstants;

// Jakarta/JEE
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

@Provider
@Slf4j
@RequiredArgsConstructor
public class ContainerContextExtractor implements ContainerRequestFilter, ContainerResponseFilter  {

	private final Instance<BsiLogProperties> bsiLog;

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    private long startTimeInMilliseconds;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		LoggingUtils.setLoggingContextField(LoggingConstants.MAIN_RESPONSE_CODE, String.valueOf(responseContext.getStatus()));
    	LoggingUtils.setLoggingContextField(LoggingConstants.MAIN_DURATION, Long.valueOf(System.currentTimeMillis() - this.startTimeInMilliseconds).toString());
        log.info(LoggingConstants.END);
        MDC.clear();
	}

	@Override
	// extract http headers as defined in the application.properties file and http verb
	// (GET, POST, PATCH, PUT...)
	public void filter(ContainerRequestContext requestContext) throws IOException {
		LoggingUtils.setLoggingContextField(LoggingConstants.MAIN_APPLICATION, appName);
		LoggingUtils.setLoggingContextField(LoggingConstants.EXECUTION_UUID, UUID.randomUUID().toString());
		startTimeInMilliseconds = System.currentTimeMillis();
		
		for (String key : requestContext.getHeaders().keySet()) {
			bsiLog.get().httpHeaders().stream().forEach(h -> {
				if (StringUtils.equalsIgnoreCase(h, key)) {
					LoggingUtils.setLoggingContextField(h, requestContext.getHeaders().get(key).get(0));
				}
			});
			if (key.equals(LoggingConstants.X_VL_REMOTE_USER)) {
				LoggingUtils.setLoggingContextField(LoggingConstants.MAIN_CLIENT, requestContext.getHeaders().get(key).get(0));
			}
		}
		LoggingUtils.setLoggingContextField(LoggingConstants.MAIN_OPERATION, requestContext.getMethod());
		//TODO validate
        log.info(LoggingConstants.START);
		LoggingUtils.setLoggingContextField("PATH", requestContext.getUriInfo().getPath());
		log.info(String.format("Request %s %s", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri()));
	}
}
