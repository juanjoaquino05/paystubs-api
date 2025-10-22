package com.atdev.paystubs_api.payment;

public record PayrollRecord(
    String fullName,
    String email,
    String position,
    double healthDiscountAmount,
    double socialDiscountAmount,
    double taxesDiscountAmount,
    double otherDiscountAmount,
    double grossSalary,
    double grossPayment,
    double netPayment,
    String period
) {}
