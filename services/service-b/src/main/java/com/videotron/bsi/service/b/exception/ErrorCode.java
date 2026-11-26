package com.videotron.bsi.service.b.exception;

import jakarta.ws.rs.core.Response.Status;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Exhaustive, immutable catalogue of BSI error codes for service-b.
 *
 * <p>Each constant groups together: the BSI code (external contract — extracted from
 * the spec, never change an existing value without a change process coordinated with
 * consuming systems), the associated default HTTP status, and the message template
 * ({@code %s}), whose expected argument count is validated when calling
 * {@link #reason(Object...)}.
 */
public enum ErrorCode {

    BAD_REQUEST("INVALID_REQUEST", Status.BAD_REQUEST, ErrorFamily.VALIDATION,
        "Invalid request: name is missing or invalid"),

/** ALWAYS use this specific code — never derive it from the analysis document. */
NOT_IMPLEMENTED("39101", Status.NOT_IMPLEMENTED, ErrorFamily.TECHNICAL, "Method not implemented: %s"),

/** ALWAYS use this code — generic catch-all, last resort only. */
GENERIC_ERROR("99999", Status.INTERNAL_SERVER_ERROR, ErrorFamily.TECHNICAL, "Unexpected error in service: %s");

    private final String code;
    private final Status defaultHttpStatus;
    private final ErrorFamily family;
    private final String reasonTemplate;
    private final int expectedArgCount;

    ErrorCode(String code, Status defaultHttpStatus, ErrorFamily family, String reasonTemplate) {
        this.code = code;
        this.defaultHttpStatus = defaultHttpStatus;
        this.family = family;
        this.reasonTemplate = reasonTemplate;
        // Inlined on purpose: a `private static final Pattern` field referenced here would be
        // an illegal forward reference (JLS 8.9.2) — enum constants are initialized before
        // any static field of the enum type. Do not hoist this back into a static field.
        this.expectedArgCount = (int) Pattern.compile("%s").matcher(reasonTemplate).results().count();
    }

    /** The BSI code (external contract). */
    public String code() {
        return code;
    }

    /** Default HTTP status; may be overridden explicitly where the exception is thrown. */
    public Status defaultHttpStatus() {
        return defaultHttpStatus;
    }

    /** Semantic family of this entry — drives log level and alerting. */
    public ErrorFamily family() {
        return family;
    }

    /** Number of {@code %s} placeholders in the reason template. */
    public int expectedArgCount() {
        return expectedArgCount;
    }

    /**
     * Formats the error message for this code.
     *
     * @param args arguments for the template — count must exactly match the number
     *             of {@code %s} placeholders declared for this code.
     * @throws IllegalArgumentException if the argument count does not match the template
     */
    public String reason(Object... args) {
        if (args.length != expectedArgCount) {
            throw new IllegalArgumentException(
                    "%s expects %d argument(s) for its reason template but %d were provided"
                            .formatted(name(), expectedArgCount, args.length));
        }
        return reasonTemplate.formatted(args);
    }

    /**
     * Resolves the {@link ErrorCode} applicable for an HTTP status received from an
     * external backend.
     *
     * @param httpStatus the HTTP status received from the backend
     * @return the matching code, or {@link #GENERIC_ERROR} if unmapped
     */
    public static ErrorCode fromHttpStatus(int httpStatus) {
        return Map.<Integer, ErrorCode>ofEntries(
                Map.entry(400, BAD_REQUEST)
                // Add entries here as needed — never mutate this map at runtime.
        ).getOrDefault(httpStatus, GENERIC_ERROR);
    }

    /**
     * Looks up an {@link ErrorCode} by its BSI textual value. For intentional aliases that share
     * a code value, this returns the first one declared.
     */
    public static Optional<ErrorCode> fromCode(String code) {
        return Arrays.stream(values())
                .filter(ec -> ec.code.equals(code))
                .findFirst();
    }
}
