package com.example.imbank.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "imbank.employee")
public class EmployeeConfig {
    private BigDecimal defaultSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
}