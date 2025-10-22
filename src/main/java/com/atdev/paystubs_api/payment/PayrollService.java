package com.atdev.paystubs_api.payment;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PayrollService {

    public List<Map<String, Object>> processCsvAndSend(Country country, String credentials, String company, MultipartFile csv) {
        return null;
    }

    private void sendEmail(PayrollRecord rec, String company, byte[] pdf, Locale loc) throws Exception {
    }
}
