package com.example.imbank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeRequestDto {
    private String fullName;
    private String email;
    private BigDecimal salary;
    private Long departmentId;
}
