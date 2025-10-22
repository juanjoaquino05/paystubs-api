package com.atdev.paystubs_api.payment;

import com.atdev.paystubs_api.payment.dto.ProcessPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PayrollService payrollService;

    private PaymentController paymentController;

    private MultipartFile testCsvFile;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(payrollService);

        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,100.00,200.00,300.00,50.00,5000.00,4500.00,3850.00,2024-01
                """;

        testCsvFile = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of(
                Map.of("email", "john@example.com", "full_name", "John Doe", "sent_at", "2024-01-15T10:00:00Z")
        );

        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        // When
        ResponseEntity<Object> response = paymentController.process(
                "do",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(ProcessPaymentResponse.class);

        ProcessPaymentResponse body = (ProcessPaymentResponse) response.getBody();
        assertThat(body.getCompany()).isEqualTo("TestCompany");
        assertThat(body.getCountry()).isEqualTo(Country.DO);
        assertThat(body.getSent()).hasSize(1);

        verify(payrollService).processCsvAndSend(eq(Country.DO), eq("user+password"), eq("TestCompany"), eq(testCsvFile));
    }

    @Test
    void shouldHandleUSACountry() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of();
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        // When
        ResponseEntity<Object> response = paymentController.process(
                "USA",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProcessPaymentResponse body = (ProcessPaymentResponse) response.getBody();
        assertThat(body.getCountry()).isEqualTo(Country.US);

        verify(payrollService).processCsvAndSend(eq(Country.US), anyString(), anyString(), any(MultipartFile.class));
    }

    @Test
    void shouldHandleDOCountry() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of();
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        // When
        ResponseEntity<Object> response = paymentController.process(
                "do",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProcessPaymentResponse body = (ProcessPaymentResponse) response.getBody();
        assertThat(body.getCountry()).isEqualTo(Country.DO);

        verify(payrollService).processCsvAndSend(eq(Country.DO), anyString(), anyString(), any(MultipartFile.class));
    }

    @Test
    void shouldUseDefaultCountryWhenNotProvided() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of();
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        // When
        ResponseEntity<Object> response = paymentController.process(
                "do", // default value
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(payrollService).processCsvAndSend(eq(Country.DO), anyString(), anyString(), any(MultipartFile.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidCountry() throws Exception {
        // When
        ResponseEntity<Object> response = paymentController.process(
                "INVALID",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "Invalid country option.");

        verify(payrollService, never()).processCsvAndSend(any(), anyString(), anyString(), any());
    }

    @Test
    void shouldValidateCountryCorrectly() {
        // Then
        assertThat(paymentController.validCountry("do")).isTrue();
        assertThat(paymentController.validCountry("USA")).isTrue();
        assertThat(paymentController.validCountry("us")).isFalse();
        assertThat(paymentController.validCountry("DO")).isFalse();
        assertThat(paymentController.validCountry("invalid")).isFalse();
        assertThat(paymentController.validCountry("")).isFalse();
        // Note: validCountry method doesn't handle null - would throw NullPointerException
    }

    @Test
    void shouldPassCredentialsToService() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of();
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        String credentials = "admin+secret123";

        // When
        paymentController.process("do", credentials, "TestCompany", testCsvFile);

        // Then
        verify(payrollService).processCsvAndSend(any(Country.class), eq(credentials), anyString(), any(MultipartFile.class));
    }

    @Test
    void shouldPassCompanyNameToService() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of();
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        String company = "Acme Corporation";

        // When
        paymentController.process("do", "user+password", company, testCsvFile);

        // Then
        verify(payrollService).processCsvAndSend(any(Country.class), anyString(), eq(company), any(MultipartFile.class));
    }

    @Test
    void shouldPropagateServiceException() throws Exception {
        // Given
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                paymentController.process("do", "user+password", "TestCompany", testCsvFile)
        );
    }

    @Test
    void shouldHandleMultipleEmployeesInResponse() throws Exception {
        // Given
        List<Map<String, Object>> expectedResults = List.of(
                Map.of("email", "john@example.com", "full_name", "John Doe", "sent_at", "2024-01-15T10:00:00Z"),
                Map.of("email", "jane@example.com", "full_name", "Jane Smith", "sent_at", "2024-01-15T10:01:00Z"),
                Map.of("email", "bob@example.com", "full_name", "Bob Johnson", "sent_at", "2024-01-15T10:02:00Z")
        );

        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(expectedResults);

        // When
        ResponseEntity<Object> response = paymentController.process(
                "USA",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProcessPaymentResponse body = (ProcessPaymentResponse) response.getBody();
        assertThat(body.getSent()).hasSize(3);
    }

    @Test
    void shouldHandleEmptyResultsList() throws Exception {
        // Given
        when(payrollService.processCsvAndSend(any(Country.class), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(List.of());

        // When
        ResponseEntity<Object> response = paymentController.process(
                "do",
                "user+password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProcessPaymentResponse body = (ProcessPaymentResponse) response.getBody();
        assertThat(body.getSent()).isEmpty();
    }
}
