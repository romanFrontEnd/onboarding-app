package com.videotron.bsi.service.b.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized constants for the service-b API.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Dictionary {

    /**
     * HTTP header names.
     */
    public static final String X_TRANSACTION_ID = "X-Transaction-Id";

    /**
     * Error representation @type value, required by the generated exception handlers.
     */
    public static final String ERROR_TYPE = "ErrorRepresentation";

    /**
     * Metric names.
     */
    public static final String HELLO_OBSERVED = "service-b.hello";
    public static final String HELLO_COUNTED = "service-b.hello.count";

    /**
     * Log message templates.
     */
    public static final String LOG_BEAN_VALIDATION_VIOLATION = "Bean validation violation: {}";
}
