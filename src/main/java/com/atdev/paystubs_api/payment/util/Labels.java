package com.atdev.paystubs_api.payment.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Labels {
    private Labels() {}

    public static Map<String, String> of(Locale locale) {
        boolean en = Locale.ENGLISH.equals(locale);
        Map<String, String> m = new HashMap<>();
        m.put("paystub_title", en ? "Paystub Payment" : "Comprobante de Pago");
        m.put("gross_salary", en ? "Gross Salary" : "Salario Bruto");
        m.put("gross_payment", en ? "Gross Payment" : "Pago Bruto");
        m.put("net_payment", en ? "Net Payment" : "Pago Neto");
        m.put("health", en ? "Health Insurance" : "SFS");
        m.put("social", en ? "Social Security" : "AFP");
        m.put("taxes", en ? "Taxes" : "ISR");
        m.put("others", en ? "Others" : "Otros");
        return m;
    }
}
