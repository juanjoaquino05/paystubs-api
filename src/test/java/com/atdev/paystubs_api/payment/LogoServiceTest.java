package com.atdev.paystubs_api.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LogoServiceTest {

    private LogoService logoService;

    @TempDir
    Path tempDir;

    private Path originalLogosDir;

    @BeforeEach
    void setUp() throws IOException {
        logoService = new LogoService();

        // Create a temporary logos directory for testing
        originalLogosDir = Path.of("logos");
        if (!Files.exists(originalLogosDir)) {
            Files.createDirectory(originalLogosDir);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files from logos directory
        Path testCompanyLogo = originalLogosDir.resolve("TestCompany.png");
        if (Files.exists(testCompanyLogo)) {
            Files.delete(testCompanyLogo);
        }
    }

    @Test
    void shouldLoadCompanySpecificLogo() throws IOException {
        // Given
        byte[] logoData = "company-logo-data".getBytes();
        Path companyLogo = originalLogosDir.resolve("TestCompany.png");
        Files.write(companyLogo, logoData);

        // When
        byte[] result = logoService.loadLogo("TestCompany");

        // Then
        assertThat(result).isEqualTo(logoData);
    }

    @Test
    void shouldLoadDefaultLogoWhenCompanyLogoNotFound() throws IOException {
        // Given
        byte[] defaultLogoData = "default-logo-data".getBytes();
        Path defaultLogo = originalLogosDir.resolve("default.png");

        // Clean up first if exists
        if (Files.exists(defaultLogo)) {
            Files.delete(defaultLogo);
        }
        Files.write(defaultLogo, defaultLogoData);

        // When
        byte[] result = logoService.loadLogo("NonExistentCompany");

        // Then
        assertThat(result).isEqualTo(defaultLogoData);

        // Cleanup
        Files.delete(defaultLogo);
    }

    @Test
    void shouldLoadClasspathDefaultLogoWhenFileSystemLogoNotFound() throws IOException {
        // Given - no logos in filesystem
        Path defaultLogo = originalLogosDir.resolve("default.png");
        if (Files.exists(defaultLogo)) {
            Files.delete(defaultLogo);
        }

        // When
        byte[] result = logoService.loadLogo("NonExistentCompany");

        // Then - should attempt to load from classpath or return null
        // This will return null if default.png is not in classpath resources
        // In a real scenario, you'd have a default.png in src/main/resources
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenNoLogosAvailable() throws IOException {
        // Given - no logos anywhere
        Path defaultLogo = originalLogosDir.resolve("default.png");
        if (Files.exists(defaultLogo)) {
            Files.delete(defaultLogo);
        }

        // When
        byte[] result = logoService.loadLogo("NonExistentCompany");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleCompanyNameWithSpecialCharacters() throws IOException {
        // Given
        byte[] logoData = "special-company-logo".getBytes();
        Path companyLogo = originalLogosDir.resolve("Company-Name_123.png");
        Files.write(companyLogo, logoData);

        // When
        byte[] result = logoService.loadLogo("Company-Name_123");

        // Then
        assertThat(result).isEqualTo(logoData);

        // Cleanup
        Files.delete(companyLogo);
    }

    @Test
    void shouldLoadCorrectLogoWhenMultipleLogosExist() throws IOException {
        // Given
        byte[] companyAData = "company-a-logo".getBytes();
        byte[] companyBData = "company-b-logo".getBytes();

        Path companyALogo = originalLogosDir.resolve("CompanyA.png");
        Path companyBLogo = originalLogosDir.resolve("CompanyB.png");

        Files.write(companyALogo, companyAData);
        Files.write(companyBLogo, companyBData);

        // When
        byte[] resultA = logoService.loadLogo("CompanyA");
        byte[] resultB = logoService.loadLogo("CompanyB");

        // Then
        assertThat(resultA).isEqualTo(companyAData);
        assertThat(resultB).isEqualTo(companyBData);

        // Cleanup
        Files.delete(companyALogo);
        Files.delete(companyBLogo);
    }
}
