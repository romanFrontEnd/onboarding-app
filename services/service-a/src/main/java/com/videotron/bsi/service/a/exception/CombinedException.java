package com.videotron.bsi.service.a.exception;

import lombok.Getter;
import lombok.Setter;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Getter
@Setter
@RegisterForReflection
public class CombinedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private transient String code;
    private transient String reason;
    private transient Integer status;

    public CombinedException(String code, String reason, Integer status) {
        super(reason);
        this.code = code;
        this.reason = reason;
        this.status = status;
    }

    public CombinedException(String code, String reason, Integer status, Throwable cause) {
        super(reason, cause);
        this.code = code;
        this.reason = reason;
        this.status = status;
    }
}
