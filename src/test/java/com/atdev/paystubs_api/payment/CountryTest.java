package com.atdev.paystubs_api.payment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountryTest {

    @Test
    void shouldConvertDoValue() {
        // When
        Country country = Country.fromValue("do");

        // Then
        assertThat(country).isEqualTo(Country.DO);
        assertThat(country.value).isEqualTo("do");
    }

    @Test
    void shouldConvertUSAValue() {
        // When
        Country country = Country.fromValue("USA");

        // Then
        assertThat(country).isEqualTo(Country.US);
        assertThat(country.value).isEqualTo("USA");
    }

    @Test
    void shouldReturnNullForInvalidValue() {
        // When
        Country country = Country.fromValue("invalid");

        // Then
        assertThat(country).isNull();
    }

    @Test
    void shouldReturnNullForNullValue() {
        // When
        Country country = Country.fromValue(null);

        // Then
        assertThat(country).isNull();
    }

    @Test
    void shouldReturnNullForEmptyString() {
        // When
        Country country = Country.fromValue("");

        // Then
        assertThat(country).isNull();
    }

    @Test
    void shouldBeCaseSensitive() {
        // When
        Country upperDO = Country.fromValue("DO");
        Country upperUSA = Country.fromValue("usa");
        Country lowerUs = Country.fromValue("us");

        // Then
        assertThat(upperDO).isNull();
        assertThat(upperUSA).isNull();
        assertThat(lowerUs).isNull();
    }

    @Test
    void shouldHaveCorrectEnumValues() {
        // When
        Country[] countries = Country.values();

        // Then
        assertThat(countries).hasSize(2);
        assertThat(countries).contains(Country.DO, Country.US);
    }

    @Test
    void shouldHaveCorrectValueForDO() {
        // Then
        assertThat(Country.DO.value).isEqualTo("do");
    }

    @Test
    void shouldHaveCorrectValueForUS() {
        // Then
        assertThat(Country.US.value).isEqualTo("USA");
    }

    @Test
    void shouldConvertToStringCorrectly() {
        // Then
        assertThat(Country.DO.toString()).isEqualTo("DO");
        assertThat(Country.US.toString()).isEqualTo("US");
    }
}
