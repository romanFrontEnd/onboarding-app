package com.videotron.bsi.service.a.businessrules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.videotron.bsi.service.a.exception.BsiException;

import java.util.stream.Stream;

/**
 * Unit tests of {@link NameValidator}: the name must be non-blank and at most 50 characters.
 *
 * @ref doc/spec/B2-5095/spec.md, Validation Process step 1 (SC-002, SC-008)
 */
@QuarkusTest
class NameValidatorTest {

    @Inject
    NameValidator validator;

    @BeforeEach
    void setUp() {
        // Nothing to prepare: the validator is stateless
    }

    @AfterEach
    void tearDown() {
        // Nothing to release
    }

    @ParameterizedTest
    @MethodSource("validNameProvider")
    void validate_validName_should_notThrow(String name) {
        assertDoesNotThrow(() -> validator.validate(name));
    }

    @ParameterizedTest
    @MethodSource("invalidNameProvider")
    void validate_invalidName_should_throwInvalidRequest(String name) {
        BsiException exception = assertThrows(BsiException.class, () -> validator.validate(name));

        assertEquals("INVALID_REQUEST", exception.getCode());
        assertEquals(400, exception.getHttpStatus());
    }

    private static Stream<Arguments> validNameProvider() {
        return Stream.of(
            Arguments.of("R"),
            Arguments.of("Roman"),
            Arguments.of("a".repeat(50))
        );
    }

    private static Stream<Arguments> invalidNameProvider() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of(""),
            Arguments.of("   "),
            Arguments.of("a".repeat(51))
        );
    }
}
