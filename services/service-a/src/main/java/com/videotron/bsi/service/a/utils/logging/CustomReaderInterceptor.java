package com.videotron.bsi.service.a.utils.logging;

// Java standard library
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Internal project packages
import com.videotron.bsi.service.a.configuration.BsiLogProperties;
import com.videotron.bsi.service.a.constants.logging.LoggingConstants;

// Jakarta/JEE
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Provider
@Slf4j
@RequiredArgsConstructor
public class CustomReaderInterceptor implements ReaderInterceptor {

	private final Instance<BsiLogProperties> bsiLog;

	private String extractRequestBody(byte buf[]) throws IOException {
		return new String(buf, StandardCharsets.UTF_8);
	}

	// intercept the request so it can be kept in the request context
	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {

		InputStream is = context.getInputStream();
		
		byte[] array = is.readAllBytes();

		if (bsiLog.get().logPayload()) {
			LoggingUtils.setLoggingContextField(LoggingConstants.PAYLOAD, extractRequestBody(array));
			log.info("log payload");
			LoggingUtils.resetLoggingContextField(LoggingConstants.PAYLOAD);
		}

		// rewrite the request body in the request context
		context.setInputStream(new ByteArrayInputStream(array));

		return context.proceed();
	}
}
