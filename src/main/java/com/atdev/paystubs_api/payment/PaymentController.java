package com.atdev.paystubs_api.payment;

import com.atdev.paystubs_api.payment.dto.ProcessPaymentResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class PaymentController {

    private final PayrollService payrollService;

    public PaymentController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> process(
            @RequestParam(name = "country", defaultValue = "do") String country,
            @RequestParam(name = "credentials") String credentials,
            @RequestParam(name = "company") String company,
            @RequestPart("file") MultipartFile csv
    ) throws Exception {

        if(!validCountry(country)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid country option.")
            );
        }

        List<Map<String, Object>> results = payrollService.processCsvAndSend(Country.valueOf(country), credentials, company, csv);

        ProcessPaymentResponse  response = ProcessPaymentResponse.builder()
                .sent(results)
                .company(company)
                .country(country)
                .build();

        return ResponseEntity.ok(response);
    }

    public boolean validCountry(String country) {
        return country.equals("do") || country.equals("USA");
    }
}
