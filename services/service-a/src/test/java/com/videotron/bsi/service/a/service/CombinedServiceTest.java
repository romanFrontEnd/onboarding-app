package com.videotron.bsi.service.a.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.videotron.bsi.service.a.businessrules.NameValidator;
import com.videotron.bsi.service.a.client.serviceb.ServiceBClientAdapter;
import com.videotron.bsi.service.a.exception.BsiException;
import com.videotron.bsi.service.a.exception.ErrorCode;
import com.videotron.bsi.service.a.model.HeaderParameter;
import com.videotron.bsi.service.a.resources.CombinedResponse;
import com.videotron.bsi.service.a.resources.HelloResponse;

/**
 * Unit tests of {@link CombinedService}.
 *
 * @ref doc/spec/B2-5095/spec.md, Validation Process, Mapping and Error Management (SC-001, SC-002, SC-005, SC-020)
 */
@QuarkusTest
class CombinedServiceTest {

    @InjectMock
    ServiceBClientAdapter serviceBClientAdapter;

    @InjectMock
    NameValidator nameValidator;

    @Inject
    CombinedService service;

    private HeaderParameter headerParameter;

    @BeforeEach
    void setUp() {
        headerParameter = HeaderParameter.builder().xTransactionId("txId").build();
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(serviceBClientAdapter, nameValidator);
    }

    @Test
    void getCombined_validName_should_returnBothBlocks() {
        // Arrange
        HelloResponse hello = new HelloResponse();
        hello.setMessage("Hello, Roman");
        hello.setService("service-b");
        hello.setVersion("1.0.0");
        when(serviceBClientAdapter.getHello("Roman")).thenReturn(hello);

        // Act
        CombinedResponse result = service.getCombined("Roman", headerParameter);

        // Assert
        assertNotNull(result);
        assertEquals("Roman", result.getName());
        assertNotNull(result.getServiceA());
        assertEquals("service-a", result.getServiceA().getService());
        assertNotNull(result.getServiceB());
        assertEquals("Hello, Roman", result.getServiceB().getMessage());
        assertEquals("1.0.0", result.getServiceB().getVersion());
        verify(nameValidator, times(1)).validate("Roman");
    }

    @Test
    void getCombined_invalidName_should_failWithoutCallingServiceB() {
        // Arrange
        doThrow(new BsiException(ErrorCode.BAD_REQUEST)).when(nameValidator).validate(any());

        // Act
        BsiException exception = assertThrows(BsiException.class,
            () -> service.getCombined(" ", headerParameter));

        // Assert
        assertEquals("INVALID_REQUEST", exception.getCode());
        verifyNoInteractions(serviceBClientAdapter);
    }

    @Test
    void getCombined_serviceBFailure_should_propagateServiceBError() {
        // Arrange
        when(serviceBClientAdapter.getHello(any()))
            .thenThrow(new BsiException(ErrorCode.BACKEND_ERROR));

        // Act
        BsiException exception = assertThrows(BsiException.class,
            () -> service.getCombined("Roman", headerParameter));

        // Assert
        assertEquals("SERVICE_B_ERROR", exception.getCode());
        assertEquals(502, exception.getHttpStatus());
    }
}
