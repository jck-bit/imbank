package com.example.imbank.controller;


import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.dto.PageResponseDto;
import com.example.imbank.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public EmployeeResponseDto createEmployee(@Valid @RequestBody EmployeeRequestDto dto) {
        return employeeService.createEmployee(dto);
    }

    @GetMapping
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/department/{departmentId}")
    public List<EmployeeResponseDto> getEmployeesByDepartment(@PathVariable Long departmentId) {
        return employeeService.getEmployeesByDepartment(departmentId);
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/salary-range")
    public List<EmployeeResponseDto> getEmployeesBySalaryRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return employeeService.getEmployeesBySalaryRange(min, max);
    }

    @GetMapping("/by-department-name")
    public List<EmployeeResponseDto> getEmployeesByDepartmentName(@RequestParam String name) {
        return employeeService.getEmployeesByDepartmentName(name);
    }

    @GetMapping("/above-average-salary")
    public List<EmployeeResponseDto> getAboveAverageSalaryEmployees() {
        return employeeService.getAboveAverageSalaryEmployees();
    }

    @GetMapping("/search")
    public List<EmployeeResponseDto> searchEmployeesByName(@RequestParam String keyword) {
        return employeeService.searchEmployeesByName(keyword);
    }

    @GetMapping("/paginated")
    public PageResponseDto<EmployeeResponseDto> getEmployeesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return employeeService.getEmployeesPaginated(page, size, sortBy, sortDir);
    }

}
