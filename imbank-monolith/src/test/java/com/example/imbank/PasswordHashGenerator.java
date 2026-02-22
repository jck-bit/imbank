package com.example.imbank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordHashGenerator {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void generatePasswordHash() {
        String password = "Admin@123";
        String hash = passwordEncoder.encode(password);
        System.out.println("===========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("===========================================");

        // Verify the hash works
        boolean matches = passwordEncoder.matches(password, hash);
        System.out.println("Hash verification: " + matches);
    }
}
