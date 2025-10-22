package com.atdev.paystubs_api.payment;

public enum Country {
    DO("do"),
    US("USA");

    public final String value;

    Country(String value) {
        this.value = value;
    }

    public static Country fromValue(String value) {
        for (Country country : Country.values()) {
            if (country.value.equals(value)) {
                return country;
            }
        }

        return null;
    }
}
