package com.atdev.paystubs_api.integration;

import com.atdev.paystubs_api.email.EmailService;
import com.atdev.paystubs_api.payment.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test that tests the full request/response cycle including security.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService; // Mock to avoid sending real emails

    @Value("${app.auth.user}")
    private String authUser;

    @Value("${app.auth.password}")
    private String authPassword;

    private MockMultipartFile testCsvFile;
    private String validCredentials;

    @BeforeEach
    void setUp() {
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

        validCredentials = authUser + "+" + authPassword;
    }

    @Test
    void shouldProcessPaymentWithValidCredentials() throws Exception {
        // Mock email service to prevent actual email sending
        doNothing().when(emailService).sendEmail(any(), any(), any(), any());

        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("country", "do")
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("TestCompany"))
                .andExpect(jsonPath("$.country").value("DO"))
                .andExpect(jsonPath("$.sent").isArray())
                .andExpect(jsonPath("$.sent[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.sent[0].full_name").value("John Doe"));

        verify(emailService, times(1)).sendEmail(any(), eq("TestCompany"), any(), any());
    }

    @Test
    void shouldRejectRequestWithInvalidCredentials() throws Exception {
        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("country", "do")
                        .param("credentials", "invalid+credentials")
                        .param("company", "TestCompany"))
                .andExpect(status().isForbidden());

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldRejectRequestWithMissingCredentials() throws Exception {
        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("country", "do")
                        .param("company", "TestCompany"))
                .andExpect(status().isForbidden());

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestForInvalidCountry() throws Exception {
        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("country", "INVALID")
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid country option."));

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldProcessPaymentForUSACountry() throws Exception {
        // Mock email service to prevent actual email sending
        doNothing().when(emailService).sendEmail(any(), any(), any(), any());

        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("country", "USA")
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("US"));

        verify(emailService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldProcessMultipleEmployees() throws Exception {
        // Mock email service to prevent actual email sending
        doNothing().when(emailService).sendEmail(any(), any(), any(), any());

        String csvWithMultipleEmployees = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                John Doe,john@example.com,Engineer,100.00,200.00,300.00,50.00,5000.00,4500.00,3850.00,2024-01
                Jane Smith,jane@example.com,Manager,150.00,250.00,400.00,75.00,7000.00,6500.00,5625.00,2024-01
                Bob Johnson,bob@example.com,Analyst,80.00,150.00,250.00,20.00,4000.00,3700.00,3200.00,2024-01
                """;

        MockMultipartFile multiEmployeeCsv = new MockMultipartFile(
                "file",
                "payroll.csv",
                "text/csv",
                csvWithMultipleEmployees.getBytes()
        );

        mockMvc.perform(multipart("/process")
                        .file(multiEmployeeCsv)
                        .param("country", "do")
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").isArray())
                .andExpect(jsonPath("$.sent.length()").value(3))
                .andExpect(jsonPath("$.sent[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.sent[1].email").value("jane@example.com"))
                .andExpect(jsonPath("$.sent[2].email").value("bob@example.com"));

        verify(emailService, times(3)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldHandleEmptyCsv() throws Exception {
        String emptyCsv = """
                full_name,email,position,health_discount_amount,social_discount_amount,taxes_discount_amount,other_discount_amount,gross_salary,gross_payment,net_payment,period
                """;

        MockMultipartFile emptyCsvFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                emptyCsv.getBytes()
        );

        mockMvc.perform(multipart("/process")
                        .file(emptyCsvFile)
                        .param("country", "do")
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").isArray())
                .andExpect(jsonPath("$.sent.length()").value(0));

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldUseDefaultCountryWhenNotProvided() throws Exception {
        // Mock email service to prevent actual email sending
        doNothing().when(emailService).sendEmail(any(), any(), any(), any());

        mockMvc.perform(multipart("/process")
                        .file(testCsvFile)
                        .param("credentials", validCredentials)
                        .param("company", "TestCompany"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("DO"));
    }
}
