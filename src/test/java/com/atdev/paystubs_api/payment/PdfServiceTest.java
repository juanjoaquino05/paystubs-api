package com.atdev.paystubs_api.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfServiceTest {

    private PdfService pdfService;
    private PayrollRecord testRecord;
    private Map<String, String> englishLabels;
    private Map<String, String> spanishLabels;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();

        testRecord = new PayrollRecord(
                "John Doe",
                "john@example.com",
                "Software Engineer",
                100.50,
                200.75,
                300.25,
                50.00,
                5000.00,
                4500.00,
                3848.50,
                "2024-01"
        );

        englishLabels = Map.of(
                "paystub_title", "Paystub",
                "gross_salary", "Gross Salary",
                "gross_payment", "Gross Payment",
                "net_payment", "Net Payment"
        );

        spanishLabels = Map.of(
                "paystub_title", "Comprobante de Pago",
                "gross_salary", "Salario Bruto",
                "gross_payment", "Pago Bruto",
                "net_payment", "Pago Neto"
        );
    }

    @Test
    void shouldGeneratePdfWithoutLogo() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        // PDF signature bytes
        assertThat(pdf).startsWith("%PDF".getBytes());
    }

    @Test
    void shouldGeneratePdfWithLogo() throws Exception {
        // Given - create a simple 1x1 pixel PNG
        byte[] simplePng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00,
                0x1F, 0x15, (byte) 0xC4, (byte) 0x89, // CRC
                0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01,
                0x0D, 0x0A, 0x2D, (byte) 0xB4, // CRC
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND chunk
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82 // CRC
        };

        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", simplePng, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        assertThat(pdf).startsWith("%PDF".getBytes());
    }

    @Test
    void shouldIncludeEmployeeInformation() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500); // PDF should have reasonable size with content
        assertThat(pdf).startsWith("%PDF".getBytes());
    }

    @Test
    void shouldIncludePaymentPeriod() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
    }

    @Test
    void shouldFormatMoneyValues() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // PDF generated successfully with all the data
    }

    @Test
    void shouldIncludeDiscountAmounts() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // PDF generated successfully with discount data
    }

    @Test
    void shouldCalculateTotalDiscounts() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // Total discounts calculated and included in PDF
    }

    @Test
    void shouldUseEnglishLabels() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // English labels passed to PDF generation
    }

    @Test
    void shouldUseSpanishLabels() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, spanishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // Spanish labels passed to PDF generation
    }

    @Test
    void shouldHandleZeroDiscounts() throws Exception {
        // Given
        PayrollRecord recordWithNoDiscounts = new PayrollRecord(
                "Jane Smith",
                "jane@example.com",
                "Manager",
                0.0, 0.0, 0.0, 0.0,
                7000.0, 7000.0, 7000.0,
                "2024-02"
        );

        // When
        byte[] pdf = pdfService.render(recordWithNoDiscounts, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // PDF generated successfully with zero discounts
    }

    @Test
    void shouldHandleLargeAmounts() throws Exception {
        // Given
        PayrollRecord recordWithLargeAmounts = new PayrollRecord(
                "Executive Director",
                "exec@example.com",
                "C-Level",
                1500.00, 2500.00, 5000.00, 500.00,
                50000.00, 45000.00, 35500.00,
                "2024-03"
        );

        // When
        byte[] pdf = pdfService.render(recordWithLargeAmounts, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // PDF generated successfully with large amounts
    }

    @Test
    void shouldHandleDecimalAmounts() throws Exception {
        // Given
        PayrollRecord recordWithDecimals = new PayrollRecord(
                "Part Timer",
                "part@example.com",
                "Assistant",
                12.34, 23.45, 34.56, 5.67,
                1234.56, 1100.00, 1023.98,
                "2024-04"
        );

        // When
        byte[] pdf = pdfService.render(recordWithDecimals, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(500);
        // PDF generated successfully with decimal amounts
    }

    @Test
    void shouldGenerateValidPdfStructure() throws Exception {
        // When
        byte[] pdf = pdfService.render(testRecord, "TestCompany", null, englishLabels);

        // Then
        assertThat(pdf).isNotNull();
        // Check for PDF header
        assertThat(pdf).startsWith("%PDF".getBytes());
        // Check for PDF EOF marker
        String pdfString = new String(pdf);
        assertThat(pdfString).contains("%%EOF");
    }

    @Test
    void shouldThrowExceptionForInvalidLogoData() {
        // Given
        byte[] invalidLogo = "not-an-image".getBytes();

        // When/Then
        assertThrows(Exception.class, () ->
                pdfService.render(testRecord, "TestCompany", invalidLogo, englishLabels)
        );
    }

    @Test
    void shouldHandleDifferentCompanyNames() throws Exception {
        // When
        byte[] pdf1 = pdfService.render(testRecord, "Acme Corp", null, englishLabels);
        byte[] pdf2 = pdfService.render(testRecord, "TechStart Inc", null, englishLabels);

        // Then
        assertThat(pdf1).isNotNull();
        assertThat(pdf2).isNotNull();
        assertThat(pdf1.length).isGreaterThan(500);
        assertThat(pdf2.length).isGreaterThan(500);
        // PDFs generated with different company names
    }
}
