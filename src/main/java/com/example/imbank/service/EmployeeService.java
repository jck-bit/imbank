package com.example.imbank.service;


import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import java.util.List;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto);
    List<EmployeeResponseDto> getAllEmployees();
    EmployeeResponseDto getEmployeeById(Long id);
    List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId);
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto employeeRequestDto);
    void deleteEmployee(Long employeeId);
}
