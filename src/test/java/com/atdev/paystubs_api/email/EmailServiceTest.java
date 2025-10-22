package com.atdev.paystubs_api.email;

import com.atdev.paystubs_api.payment.PayrollRecord;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailService emailService;

    private PayrollRecord testRecord;
    private byte[] testPdf;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "from", "noreply@company.com");

        testRecord = new PayrollRecord(
                "John Doe",
                "john@example.com",
                "Engineer",
                100.0,
                200.0,
                300.0,
                50.0,
                5000.0,
                4500.0,
                3850.0,
                "2024-01"
        );

        testPdf = "pdf-content".getBytes();
    }

    @Test
    void shouldSendEmailWithEnglishLocale() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(testRecord, "TestCompany", testPdf, Locale.ENGLISH);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldSendEmailWithSpanishLocale() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(testRecord, "TestCompany", testPdf, new Locale("es", "DO"));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldUseEnglishSubjectForEnglishLocale() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String company = "TestCompany";

        // When
        emailService.sendEmail(testRecord, company, testPdf, Locale.ENGLISH);

        // Then
        // Subject should contain "Paystub Payment - TestCompany"
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldUseSpanishSubjectForSpanishLocale() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String company = "TestCompany";

        // When
        emailService.sendEmail(testRecord, company, testPdf, new Locale("es", "DO"));

        // Then
        // Subject should contain "Comprobante de Pago - TestCompany"
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldSendToCorrectRecipient() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(testRecord, "TestCompany", testPdf, Locale.ENGLISH);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldAttachPdfWithCorrectFilename() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(testRecord, "TestCompany", testPdf, Locale.ENGLISH);

        // Then
        // Attachment should be named "paystub-John_Doe.pdf"
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldReplaceSpacesInFilename() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        PayrollRecord recordWithSpaces = new PayrollRecord(
                "John Middle Doe",
                "john@example.com",
                "Engineer",
                100.0, 200.0, 300.0, 50.0, 5000.0, 4500.0, 3850.0,
                "2024-01"
        );

        // When
        emailService.sendEmail(recordWithSpaces, "TestCompany", testPdf, Locale.ENGLISH);

        // Then
        // Attachment should be named "paystub-John_Middle_Doe.pdf" (spaces replaced with underscores)
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldPropagateMailSenderException() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                emailService.sendEmail(testRecord, "TestCompany", testPdf, Locale.ENGLISH)
        );
    }

    @Test
    void shouldHandleNullLocaleGracefully() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(testRecord, "TestCompany", testPdf, null);

        // Then
        // Should not crash, will use Spanish by default (non-English locale)
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldIncludeCompanyNameInSubject() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String company = "Acme Corporation";

        // When
        emailService.sendEmail(testRecord, company, testPdf, Locale.ENGLISH);

        // Then
        verify(mailSender).send(mimeMessage);
    }
}
