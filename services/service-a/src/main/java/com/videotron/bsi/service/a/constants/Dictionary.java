    package com.videotron.bsi.service.a.constants;

    import java.util.List;

import lombok.AccessLevel;
    import lombok.NoArgsConstructor;

    /**
     * Centralized constants for the service-a API.
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
        public static final String COMBINED_OBSERVED = "service-a.combined";
        public static final String COMBINED_COUNTED = "service-a.combined.count";

        /**
         * Log message templates.
         */
        public static final String LOG_BEAN_VALIDATION_VIOLATION = "Bean validation violation: {}";

    /**
     * Backend names and processes.
     */
    public static final String SERVICE_B = "service-b";

    /**
     * Headers propagated to the backend.
     */
    public static final List<String> CORRELATION_HEADERS = List.of(X_TRANSACTION_ID);

    /**
     * Log message templates for the backend exception handling.
     */
    public static final String LOG_BACKEND_ERROR_BODY = "Backend {} answered HTTP {} code={} reason={} transactionId={}";
    public static final String LOG_BACKEND_ERROR_STATUS = "Backend {} answered HTTP {} transactionId={}";
    public static final String LOG_BACKEND_ERROR_UNREADABLE = "Backend {} answered HTTP {} with an unreadable error body";
    public static final String LOG_BACKEND_CALL_FAILED = "Call to backend {} failed";
    public static final String LOG_MAPPER_ERROR = "Domain exception translated to an error response";
}
