package com.atdev.paystubs_api.payment;

public enum Country {
    DO("do"),
    US("USA");

    public final String value;

    Country(String value) {
        this.value = value;
    }
}
