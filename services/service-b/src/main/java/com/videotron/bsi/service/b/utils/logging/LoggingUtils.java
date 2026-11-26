package com.videotron.bsi.service.b.utils.logging;

// Java standard library
import java.util.List;
import java.util.Map;

// Third-party libraries
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class LoggingUtils {

    public static void setLoggingContextField(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    public static void setLoggingContextFieldList(Map<String, String> values) {
        if (values == null) {
            return;
        }
        for (Map.Entry<String, String> pair : values.entrySet()) {
            setLoggingContextField(pair.getKey(), pair.getValue());
        }
    }
    
    public static void resetLoggingContextField(String key) {
    	if (StringUtils.isNotBlank(key)) {
    		MDC.remove(key);
    	}
    }
    
    public static void resetLoggingContextFieldList(List<String> keys) {
        if (keys == null) {
            return;
        }
        keys.forEach(k -> resetLoggingContextField(k));
    }
}
