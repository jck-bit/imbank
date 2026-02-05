package com.example.imbank.service;


import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.dto.PageResponseDto;

import java.util.List;
import java.math.BigDecimal;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto);
    List<EmployeeResponseDto> getAllEmployees();
    EmployeeResponseDto getEmployeeById(Long id);
    List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId);
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto employeeRequestDto);
    void deleteEmployee(Long employeeId);


    List<EmployeeResponseDto> getEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary);
    List<EmployeeResponseDto> getEmployeesByDepartmentName(String departmentName);
    List<EmployeeResponseDto> getAboveAverageSalaryEmployees();
    List<EmployeeResponseDto> searchEmployeesByName(String keyword);


    // Pagination & Sorting
    PageResponseDto<EmployeeResponseDto> getEmployeesPaginated(int page, int size, String sortBy, String sortDir);
}
