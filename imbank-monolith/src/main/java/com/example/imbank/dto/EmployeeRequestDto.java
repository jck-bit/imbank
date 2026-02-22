package com.example.imbank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeRequestDto {
    private String fullName;

    @NotBlank(message="Email is required")
    @Email(message = "Invalid Email format")
    private String email;
    private BigDecimal salary;

    @NotNull(message = "Department Id is required")
    private Long departmentId;
}
