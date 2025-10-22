package com.atdev.paystubs_api.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvServiceTest {

    private CsvService csvService;

    @BeforeEach
    void setUp() {
        csvService = new CsvService();
    }

    @Test
    void shouldParseValidCsvWithAllFields() throws Exception {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,100.50,200.75,300.25,50.00,5000.00,4500.00,3848.50,2024-01
                Jane Smith,jane@example.com,Manager,150.00,250.00,400.00,75.00,7000.00,6500.00,5625.00,2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).hasSize(2);

        PayrollRecord first = records.get(0);
        assertThat(first.fullName()).isEqualTo("John Doe");
        assertThat(first.email()).isEqualTo("john@example.com");
        assertThat(first.position()).isEqualTo("Engineer");
        assertThat(first.healthDiscountAmount()).isEqualTo(100.50);
        assertThat(first.socialDiscountAmount()).isEqualTo(200.75);
        assertThat(first.taxesDiscountAmount()).isEqualTo(300.25);
        assertThat(first.otherDiscountAmount()).isEqualTo(50.00);
        assertThat(first.grossSalary()).isEqualTo(5000.00);
        assertThat(first.grossPayment()).isEqualTo(4500.00);
        assertThat(first.netPayment()).isEqualTo(3848.50);
        assertThat(first.period()).isEqualTo("2024-01");

        PayrollRecord second = records.get(1);
        assertThat(second.fullName()).isEqualTo("Jane Smith");
        assertThat(second.email()).isEqualTo("jane@example.com");
        assertThat(second.position()).isEqualTo("Manager");
    }

    @Test
    void shouldParseEmptyDiscountFieldsAsZero() throws Exception {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,,,,,5000.00,5000.00,5000.00,2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).hasSize(1);
        PayrollRecord record = records.get(0);
        assertThat(record.healthDiscountAmount()).isZero();
        assertThat(record.socialDiscountAmount()).isZero();
        assertThat(record.taxesDiscountAmount()).isZero();
        assertThat(record.otherDiscountAmount()).isZero();
    }

    @Test
    void shouldParseBlankDiscountFieldsAsZero() throws Exception {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,  ,  ,  ,  ,5000.00,5000.00,5000.00,2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).hasSize(1);
        PayrollRecord record = records.get(0);
        assertThat(record.healthDiscountAmount()).isZero();
        assertThat(record.socialDiscountAmount()).isZero();
        assertThat(record.taxesDiscountAmount()).isZero();
        assertThat(record.otherDiscountAmount()).isZero();
    }

    @Test
    void shouldHandleCaseInsensitiveHeaders() throws Exception {
        // Given
        String csvContent = """
                FULL_NAME,EMAIL,POSITION,HEALTH_DISCOUNT_AMOUNT,SOCIAL_DISCOUNT_AMOUNT,TAXES_DISCOUNT_AMOUNT,OTHER_DISCOUNT_AMOUNT,GROSS_SALARY,GROSS_PAYMENT,NET_PAYMENT,PERIOD
                John Doe,john@example.com,Engineer,100.00,200.00,300.00,50.00,5000.00,4500.00,3850.00,2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).hasSize(1);
        assertThat(records.get(0).fullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldHandleExtraWhitespaceInValues() throws Exception {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                  John Doe  ,  john@example.com  ,  Engineer  ,100.00,200.00,300.00,50.00,5000.00,4500.00,3850.00,  2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).hasSize(1);
        PayrollRecord record = records.get(0);
        assertThat(record.fullName()).isEqualTo("John Doe");
        assertThat(record.email()).isEqualTo("john@example.com");
        assertThat(record.position()).isEqualTo("Engineer");
        assertThat(record.period()).isEqualTo("2024-01");
    }

    @Test
    void shouldParseEmptyCsvWithHeadersOnly() throws Exception {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When
        List<PayrollRecord> records = csvService.parse(file);

        // Then
        assertThat(records).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenMissingRequiredHeaders() {
        // Given
        String csvContent = """
                name,email
                John Doe,john@example.com
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When/Then
        assertThrows(Exception.class, () -> csvService.parse(file));
    }

    @Test
    void shouldThrowExceptionWhenInvalidNumberFormat() {
        // Given
        String csvContent = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,invalid,200.00,300.00,50.00,5000.00,4500.00,3850.00,2024-01
                """;
        MultipartFile file = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // When/Then
        assertThrows(Exception.class, () -> csvService.parse(file));
    }
}
