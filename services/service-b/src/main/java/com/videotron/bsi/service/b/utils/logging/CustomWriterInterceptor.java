package com.videotron.bsi.service.b.utils.logging;

// Java standard library
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// Internal project packages
import com.videotron.bsi.service.b.configuration.BsiLogProperties;
import com.videotron.bsi.service.b.constants.logging.LoggingConstants;

// Jakarta/JEE
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Provider
@Slf4j
@RequiredArgsConstructor
public class CustomWriterInterceptor implements WriterInterceptor {

	private final Instance<BsiLogProperties> bsiLog;

	// intercept the response so it can be kept in the logging context
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        OutputStream originalStream = context.getOutputStream();
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream baos;
        if (originalStream instanceof ByteArrayOutputStream) { // rest client
            baos = (ByteArrayOutputStream) originalStream;
        } else {
            baos = new ByteArrayOutputStream();
        }
        
        context.setOutputStream(baos);
        try {
            context.proceed();
        } finally {
        	if (bsiLog.get().logPayload()) {
                LoggingUtils.setLoggingContextField(LoggingConstants.PAYLOAD, baos.toString("UTF-8"));
    			log.info("log payload");
                LoggingUtils.resetLoggingContextField(LoggingConstants.PAYLOAD);
        	}
        	// rewrite the output in the context
        	baos.writeTo(originalStream);
            baos.close();
            context.setOutputStream(originalStream);
        }
    }
}
