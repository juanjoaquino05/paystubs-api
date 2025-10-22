package com.atdev.paystubs_api.email;

import com.atdev.paystubs_api.payment.PayrollRecord;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(PayrollRecord rec, String company, byte[] pdf, Locale loc) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(rec.email());
        String subject = (Locale.ENGLISH.equals(loc) ? "Paystub Payment" : "Comprobante de Pago") + " - " + company;
        helper.setSubject(subject);
        helper.setText((Locale.ENGLISH.equals(loc) ? "Please find attached your paystub." : "Adjunto su comprobante de pago."), false);
        helper.addAttachment("paystub-" + rec.fullName().replaceAll("\\s+", "_") + ".pdf", new ByteArrayResource(pdf));
        mailSender.send(message);
    }
}
