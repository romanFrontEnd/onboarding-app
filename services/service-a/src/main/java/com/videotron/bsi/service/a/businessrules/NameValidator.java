package com.videotron.bsi.service.a.businessrules;

import static com.videotron.bsi.service.a.function.BsiExceptionFunction.specifyInvalidRequest;

// Third-party libraries
import org.apache.commons.lang3.StringUtils;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;

/**
 * The name must be non-blank and at most 50 characters (spec: Validation Process, step 1).
 *
 * @traced NameValidatorTest#validate_validName_should_notThrow, NameValidatorTest#validate_invalidName_should_throwInvalidRequest
 * @ref doc/spec/B2-5095/spec.md, Validation Process step 1 (SC-002, SC-008)
 */
@ApplicationScoped
public class NameValidator {

    private static final int NAME_MAX_LENGTH = 50;

    /**
     * @param name the name received on the request
     * @throws com.videotron.bsi.service.a.exception.BsiException 400/INVALID_REQUEST when the name is blank
     *         or longer than 50 characters
     */
    public void validate(String name) {
        // Traced: NameValidatorTest#validate_invalidName_should_throwInvalidRequest
        // Ref: spec Validation Process step 1 — non-blank, at most 50 characters
        if (StringUtils.isBlank(name) || name.length() > NAME_MAX_LENGTH) {
            throw specifyInvalidRequest.get();
        }
    }
}
