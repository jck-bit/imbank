package com.example.imbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication

public class ImbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImbankApplication.class, args);
    }

}
