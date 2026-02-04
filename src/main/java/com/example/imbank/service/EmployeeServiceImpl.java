package com.example.imbank.service;
import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.entity.Department;
import com.example.imbank.entity.Employee;
import com.example.imbank.repository.DepartmentRepository;
import com.example.imbank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto ){
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);

        return new EmployeeResponseDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getDepartment().getName()
        );
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees(){
        return employeeRepository.findAll()
                .stream()
                .map(employee -> new EmployeeResponseDto(
                        employee.getId(),
                        employee.getFullName(),
                        employee.getEmail(),
                        employee.getDepartment().getName()
                ))
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> getEmployeesByDepartment(Long departmentId){
        return employeeRepository.findByDepartment_Id(departmentId)
                .stream()
                .map(employee -> new EmployeeResponseDto(
                        employee.getId(),
                        employee.getFullName(),
                        employee.getEmail(),
                        employee.getDepartment().getName()
                ))
                .toList();
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toResponseDto(employee);
    }

    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);

        Employee updated = employeeRepository.save(employee);
        return toResponseDto(updated);
    }

    @Override
    public void deleteEmployee(Long id){
        if(!employeeRepository.existsById(id)){
            throw new RuntimeException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    private EmployeeResponseDto toResponseDto(Employee employee){
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDepartment().getName()
        );
    }

}
