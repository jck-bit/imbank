package com.example.imbank.service;

import com.example.imbank.dto.DepartmentRequestDto;
import com.example.imbank.dto.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {
    DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto);
    List<DepartmentResponseDto> getAllDepartments();
    DepartmentResponseDto getDepartmentById(Long id);
    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto);
    void deleteDepartment(Long id);
}
