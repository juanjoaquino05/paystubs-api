package com.atdev.paystubs_api.payment;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LogoService {
    private static final Path FS_LOGO_DIR = Path.of("logos");

    public byte[] loadLogo(final String company) throws IOException {
        Path p = FS_LOGO_DIR.resolve(company + ".png");
        if (Files.exists(p)) return Files.readAllBytes(p);

        Path def = FS_LOGO_DIR.resolve("default.png");
        if (Files.exists(def)) return Files.readAllBytes(def);

        try {
            var res = new ClassPathResource("default.png");
            if (res.exists()) return res.getContentAsByteArray();
        } catch (Exception ignored) {}
        return null;
    }
}
