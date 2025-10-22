package com.atdev.paystubs_api.payment;

import com.atdev.paystubs_api.payment.util.Labels;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class PayrollService {
    private final CsvService csvService;
    private final PdfService pdfService;
    private final LogoService logoService;

    public List<Map<String, Object>> processCsvAndSend(Country country, String credentials, String company, MultipartFile csv) throws Exception {
        Locale locale = switch (country.toString().toLowerCase()) {
            case "usa", "en", "us" -> Locale.ENGLISH;
            default -> new Locale("es", "DO");
        };
        List<PayrollRecord> rows = csvService.parse(csv);
        byte[] logo = logoService.loadLogo(company);

        List<Map<String, Object>> sent = new ArrayList<>();
        for (PayrollRecord rec : rows) {
            byte[] pdf = pdfService.render(rec, company, logo, Labels.of(locale));
            sendEmail(rec, company, pdf, locale);
            sent.add(Map.of(
                    "email", rec.email(),
                    "full_name", rec.fullName(),
                    "sent_at", OffsetDateTime.now().toString()
            ));
        }

        return sent;
    }

    private void sendEmail(PayrollRecord rec, String company, byte[] pdf, Locale loc) throws Exception {
    }
}
