package com.atdev.paystubs_api.payment;

import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CsvService {
    public List<PayrollRecord> parse(MultipartFile csvFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {
            List<PayrollRecord> list = new ArrayList<>();
            for (CSVRecord r : parser) {
                list.add(new PayrollRecord(
                        r.get("full_name"),
                        r.get("email"),
                        r.get("position"),
                        parseD(r.get("health_discount_amount")),
                        parseD(r.get("social_discount_amount")),
                        parseD(r.get("taxes_discount_amount")),
                        parseD(r.get("other_discount_amount")),
                        parseD(r.get("gross_salary")),
                        parseD(r.get("gross_payment")),
                        parseD(r.get("net_payment")),
                        r.get("period")
                ));
            }
            return list;
        }
    }

    private double parseD(String s) { return s == null || s.isBlank() ? 0d : Double.parseDouble(s); }
}
