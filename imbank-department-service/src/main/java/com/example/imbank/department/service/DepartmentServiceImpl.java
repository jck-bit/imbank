package com.example.imbank.department.service;


import com.example.imbank.department.dto.DepartmentRequestDto;
import com.example.imbank.department.dto.DepartmentResponseDto;
import com.example.imbank.department.entity.Department;
import com.example.imbank.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.imbank.department.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto dto) {
        log.info("Creating department with name: {}", dto.getName());

        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());

        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with id: {}", saved.getId());

        return toResponseDto(saved);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments() {
        log.debug("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        log.debug("Found {} departments", departments.size());

        return departments.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        log.debug("Fetching department with id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department Not found with id: {}", id);
                    return new ResourceNotFoundException("Department", "id", id);
                });

        return toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto) {
        log.info("Updating department with id: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with id: {}", id);
                    return new ResourceNotFoundException("Department", "id", id);
                });

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());

        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully with id: {}", updated.getId());

     //   return toResponseDto(updated);
        return null;
    }

    @Override
    public void deleteDepartment(Long id){
        if(!departmentRepository.existsById(id)){
            throw new ResourceNotFoundException("Department", "id", id);
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
