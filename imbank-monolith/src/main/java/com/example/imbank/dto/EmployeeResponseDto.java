package com.example.imbank.dto;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class EmployeeResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String departmentName;
}
