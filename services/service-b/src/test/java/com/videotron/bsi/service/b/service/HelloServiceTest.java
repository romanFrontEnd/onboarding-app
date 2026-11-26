package com.videotron.bsi.service.b.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.videotron.bsi.service.b.model.HeaderParameter;
import com.videotron.bsi.service.b.resources.HelloResponse;

/**
 * Unit tests of {@link HelloService}.
 *
 * @ref doc/spec/B2-5095/spec.md, API contract (B) and SC-010
 */
@QuarkusTest
class HelloServiceTest {

    @Inject
    HelloService service;

    private HeaderParameter headerParameter;

    @BeforeEach
    void setUp() {
        headerParameter = HeaderParameter.builder().xTransactionId("txId").build();
    }

    @AfterEach
    void tearDown() {
        headerParameter = null;
    }

    @Test
    void getHello_validName_should_returnGreetingFromServiceB() {
        // Arrange
        String name = "Roman";

        // Act
        HelloResponse result = service.getHello(name, headerParameter);

        // Assert
        assertNotNull(result);
        assertEquals("Hello, Roman", result.getMessage());
        assertEquals("service-b", result.getService());
        assertEquals("1.0.0", result.getVersion());
    }
}
