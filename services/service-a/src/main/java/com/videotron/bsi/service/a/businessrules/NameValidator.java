package com.videotron.bsi.service.a.businessrules;

// Third-party libraries
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

// Internal project packages
import com.videotron.bsi.service.a.exception.CombinedException;
import static com.videotron.bsi.service.a.exception.ErrorCode.NOT_IMPLEMENTED;

/**
 * The name must be non-blank and at most 50 characters (spec: Validation Process, step 1).
 */
@ApplicationScoped
public class NameValidator {

    public void validate(String name) {
        // TODO Not Implemented — Cody implements from failing tests (spec: Validation Process)
        throw new CombinedException(
            NOT_IMPLEMENTED.code(),
            NOT_IMPLEMENTED.reason("validate"),
            Status.NOT_IMPLEMENTED.getStatusCode()
        );
    }
}
