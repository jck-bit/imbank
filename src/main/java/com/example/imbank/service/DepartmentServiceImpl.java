package com.example.imbank.service;


import com.example.imbank.dto.DepartmentRequestDto;
import com.example.imbank.dto.DepartmentResponseDto;
import com.example.imbank.entity.Department;
import com.example.imbank.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponseDto createDepartment (DepartmentRequestDto dto){
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());

        Department saved = departmentRepository.save(department);
        return toResponseDto(saved);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments(){
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Department Not Found!"
                ));
        return toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto departmentRequestDto){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department Not Found!"));

        department.setName(departmentRequestDto.getName());
        department.setDescription(departmentRequestDto.getDescription());

        Department updated = departmentRepository.save(department);
        return toResponseDto(updated);
    }

    @Override
    public void deleteDepartment(Long id){
        if(!departmentRepository.existsById(id)){
            throw new RuntimeException("Department Not Found!");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponseDto toResponseDto(Department department){
        return new DepartmentResponseDto(
                department.getId(),
                department.getName(),
                department.getDescription());
    }



}
