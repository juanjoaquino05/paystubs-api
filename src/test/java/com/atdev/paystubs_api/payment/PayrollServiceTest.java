package com.atdev.paystubs_api.payment;

import com.atdev.paystubs_api.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private CsvService csvService;

    @Mock
    private PdfService pdfService;

    @Mock
    private LogoService logoService;

    @Mock
    private EmailService emailService;

    private PayrollService payrollService;

    private MultipartFile testCsvFile;
    private List<PayrollRecord> testRecords;
    private byte[] testLogo;
    private byte[] testPdf;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollService(csvService, pdfService, logoService, emailService);

        testCsvFile = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                "test,data".getBytes()
        );

        testRecords = List.of(
                new PayrollRecord(
                        "John Doe",
                        "john@example.com",
                        "Engineer",
                        100.0, 200.0, 300.0, 50.0,
                        5000.0, 4500.0, 3850.0,
                        "2024-01"
                ),
                new PayrollRecord(
                        "Jane Smith",
                        "jane@example.com",
                        "Manager",
                        150.0, 250.0, 400.0, 75.0,
                        7000.0, 6500.0, 5625.0,
                        "2024-01"
                )
        );

        testLogo = "logo-data".getBytes();
        testPdf = "pdf-data".getBytes();
    }

    @Test
    void shouldProcessCsvAndSendEmailsForAllRecords() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo("TestCompany")).thenReturn(testLogo);
        when(pdfService.render(any(PayrollRecord.class), eq("TestCompany"), eq(testLogo), any()))
                .thenReturn(testPdf);

        // When
        List<Map<String, Object>> result = payrollService.processCsvAndSend(
                Country.US,
                "user:password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(result).hasSize(2);

        verify(csvService).parse(testCsvFile);
        verify(logoService).loadLogo("TestCompany");
        verify(pdfService, times(2)).render(any(PayrollRecord.class), eq("TestCompany"), eq(testLogo), any());
        verify(emailService, times(2)).sendEmail(any(PayrollRecord.class), eq("TestCompany"), eq(testPdf), any(Locale.class));
    }

    @Test
    void shouldUseEnglishLocaleForUSCountry() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);

        // When
        payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile);

        // Then
        verify(emailService, times(2)).sendEmail(
                any(PayrollRecord.class),
                anyString(),
                any(byte[].class),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void shouldUseSpanishLocaleForDOCountry() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);

        // When
        payrollService.processCsvAndSend(Country.DO, "user:password", "TestCompany", testCsvFile);

        // Then
        verify(emailService, times(2)).sendEmail(
                any(PayrollRecord.class),
                anyString(),
                any(byte[].class),
                eq(new Locale("es", "DO"))
        );
    }

    @Test
    void shouldReturnListOfSentEmails() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);

        // When
        List<Map<String, Object>> result = payrollService.processCsvAndSend(
                Country.US,
                "user:password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(result).hasSize(2);

        Map<String, Object> first = result.get(0);
        assertThat(first).containsKeys("email", "full_name", "sent_at");
        assertThat(first.get("email")).isEqualTo("john@example.com");
        assertThat(first.get("full_name")).isEqualTo("John Doe");
        assertThat(first.get("sent_at")).isNotNull();

        Map<String, Object> second = result.get(1);
        assertThat(second.get("email")).isEqualTo("jane@example.com");
        assertThat(second.get("full_name")).isEqualTo("Jane Smith");
    }

    @Test
    void shouldProcessEmptyListWhenNoRecords() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(List.of());
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);

        // When
        List<Map<String, Object>> result = payrollService.processCsvAndSend(
                Country.US,
                "user:password",
                "TestCompany",
                testCsvFile
        );

        // Then
        assertThat(result).isEmpty();
        verify(pdfService, never()).render(any(), anyString(), any(), any());
        verify(emailService, never()).sendEmail(any(), anyString(), any(), any());
    }

    @Test
    void shouldPropagateExceptionFromCsvService() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenThrow(new RuntimeException("CSV parse error"));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile)
        );

        verify(logoService, never()).loadLogo(anyString());
        verify(pdfService, never()).render(any(), anyString(), any(), any());
        verify(emailService, never()).sendEmail(any(), anyString(), any(), any());
    }

    @Test
    void shouldPropagateExceptionFromLogoService() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenThrow(new RuntimeException("Logo load error"));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile)
        );

        verify(csvService).parse(testCsvFile);
        verify(pdfService, never()).render(any(), anyString(), any(), any());
        verify(emailService, never()).sendEmail(any(), anyString(), any(), any());
    }

    @Test
    void shouldPropagateExceptionFromPdfService() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("PDF generation error"));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile)
        );

        verify(csvService).parse(testCsvFile);
        verify(logoService).loadLogo(anyString());
        verify(emailService, never()).sendEmail(any(), anyString(), any(), any());
    }

    @Test
    void shouldPropagateExceptionFromEmailService() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);
        doThrow(new RuntimeException("Email send error"))
                .when(emailService).sendEmail(any(), anyString(), any(), any());

        // When/Then
        assertThrows(RuntimeException.class, () ->
                payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile)
        );

        verify(csvService).parse(testCsvFile);
        verify(logoService).loadLogo(anyString());
        verify(pdfService).render(any(), anyString(), any(), any());
    }

    @Test
    void shouldLoadLogoOncePerBatch() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo("TestCompany")).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);

        // When
        payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile);

        // Then
        verify(logoService, times(1)).loadLogo("TestCompany");
    }

    @Test
    void shouldGenerateSeparatePdfForEachRecord() throws Exception {
        // Given
        when(csvService.parse(testCsvFile)).thenReturn(testRecords);
        when(logoService.loadLogo(anyString())).thenReturn(testLogo);
        when(pdfService.render(any(), anyString(), any(), any())).thenReturn(testPdf);

        // When
        payrollService.processCsvAndSend(Country.US, "user:password", "TestCompany", testCsvFile);

        // Then
        verify(pdfService).render(eq(testRecords.get(0)), eq("TestCompany"), eq(testLogo), any());
        verify(pdfService).render(eq(testRecords.get(1)), eq("TestCompany"), eq(testLogo), any());
    }
}
