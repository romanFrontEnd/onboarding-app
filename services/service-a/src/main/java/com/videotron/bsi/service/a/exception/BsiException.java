package com.videotron.bsi.service.a.exception;

/** Runtime exception carrying an ErrorCode catalog entry and its template args.
 *  Status and reason default to the catalog entry; overridden via relayed(...). */
public class BsiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] messageArgs;
    private final Integer httpStatus;
    private final String reason;

    /** Catalog error with template arguments (arity enforced by ErrorCode.reason). */
    public BsiException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.reason(messageArgs));
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
        this.httpStatus = null;
        this.reason = null;
    }

    /** Catalog error preserving the cause — causes are wrapped, never lost. */
    public BsiException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode.reason(messageArgs), cause);
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
        this.httpStatus = null;
        this.reason = null;
    }

    private BsiException(ErrorCode errorCode, String reason, int httpStatus) {
        super(reason);
        this.errorCode = errorCode;
        this.messageArgs = new Object[0];
        this.httpStatus = httpStatus;
        this.reason = reason;
    }

    /** Explicit status and reason instead of the catalog-derived ones. */
    public static BsiException relayed(ErrorCode errorCode, String reason, int httpStatus) {
        return new BsiException(errorCode, reason, httpStatus);
    }

    public ErrorCode getErrorCode() { return errorCode; }

    /** BSI business code of the catalog entry. */
    public String getCode() { return errorCode.code(); }

    /** Explicit HTTP status when set, the catalog default otherwise. */
    public int getHttpStatus() {
        return null != httpStatus ? httpStatus : errorCode.defaultHttpStatus().getStatusCode();
    }

    /** Explicit reason when set, the formatted catalog template otherwise. */
    public String getReason() {
        return null != reason ? reason : errorCode.reason(messageArgs);
    }
}
