package com.videotron.bsi.service.b.exception;

import lombok.Getter;
import lombok.Setter;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Getter
@Setter
@RegisterForReflection
public class HelloException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private transient String code;
    private transient String reason;
    private transient Integer status;

    public HelloException(String code, String reason, Integer status) {
        super(reason);
        this.code = code;
        this.reason = reason;
        this.status = status;
    }

    public HelloException(String code, String reason, Integer status, Throwable cause) {
        super(reason, cause);
        this.code = code;
        this.reason = reason;
        this.status = status;
    }
}
