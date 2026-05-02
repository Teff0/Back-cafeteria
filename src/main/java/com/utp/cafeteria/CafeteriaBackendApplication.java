package com.utp.cafeteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
public class CafeteriaBackendApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(CafeteriaBackendApplication.class, args);
    }

    private static void loadEnvFile() {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                props.load(fis);
                props.forEach((key, value) -> 
                    System.setProperty(key.toString(), value.toString())
                );
                System.out.println("[INFO] Loaded .env file");
            } catch (IOException e) {
                System.out.println("[WARN] Could not load .env file: " + e.getMessage());
            }
        }
    }
}