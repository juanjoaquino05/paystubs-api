package com.atdev.paystubs_api.payment.dto;

import com.atdev.paystubs_api.payment.Country;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentResponse {
    private String company;
    private Country country;
    private String processed_at = OffsetDateTime.now().toString();
    private List<Map<String, Object>> sent;
}
