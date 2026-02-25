package com.example.imbank.department.service;

import com.example.imbank.department.dto.DepartmentRequestDto;
import com.example.imbank.department.dto.DepartmentResponseDto;
import java.util.List;

public interface DepartmentService {
    DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto);
    List<DepartmentResponseDto> getAllDepartments();
    DepartmentResponseDto getDepartmentById(Long id);
    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto);
    void deleteDepartment(Long id);
}
