package com.example.imbank.controller;


import com.example.imbank.dto.DepartmentRequestDto;
import com.example.imbank.dto.DepartmentResponseDto;
import com.example.imbank.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;


@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor

public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    public DepartmentResponseDto createDepartment(@Valid @RequestBody DepartmentRequestDto dto) {
        return departmentService.createDepartment(dto);
    }

    @GetMapping
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentResponseDto getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PutMapping("/{id}")
    public DepartmentResponseDto updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDto dto) {
        return departmentService.updateDepartment(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }
}
