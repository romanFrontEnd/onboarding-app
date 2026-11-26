package com.videotron.bsi.service.b.exception;

/** Semantic family of a catalog entry — drives log level and alerting. */
public enum ErrorFamily {
    VALIDATION,       // consumer-caused rejections -> WARN
    AUTHENTICATION,   // missing/rejected credentials -> WARN
    AUTHORIZATION,    // authenticated but not allowed -> WARN
    NOT_FOUND,        // resource not found -> WARN
    CONFLICT,         // state conflict -> WARN
    RATE_LIMIT,       // throttling -> WARN
    TECHNICAL,        // backend/technical failures -> ERROR
    EXTERNAL          // external dependency unavailable -> ERROR
}
