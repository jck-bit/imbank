package com.example.imbank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PasswordVerificationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void verifyAdminPasswordHash() {
        String plainPassword = "Admin@123";
        String storedHash = "$2a$10$Y/Tm37Rr/Els3elYryl7Hezg60bFVAbqvkRbBeqvWjfBKyivSOA5i";

        System.out.println("===========================================");
        System.out.println("Testing password verification:");
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Stored hash:    " + storedHash);

        boolean matches = passwordEncoder.matches(plainPassword, storedHash);

        System.out.println("Matches: " + matches);
        System.out.println("===========================================");

        assertTrue(matches, "Password should match the hash!");
    }

    @Test
    public void checkBCryptConfiguration() {
        // Generate a new hash to compare
        String password = "Admin@123";
        String newHash = passwordEncoder.encode(password);

        System.out.println("===========================================");
        System.out.println("BCrypt Configuration Test:");
        System.out.println("Password: " + password);
        System.out.println("New hash: " + newHash);

        // Extract salt rounds from the hash
        String[] parts = newHash.split("\\$");
        System.out.println("Algorithm: $" + parts[1]);
        System.out.println("Rounds: " + parts[2]);

        // Verify it matches
        boolean matches = passwordEncoder.matches(password, newHash);
        System.out.println("Verification: " + matches);
        System.out.println("===========================================");

        assertTrue(matches);
    }
}
