package com.example.imbank.service;
import com.example.imbank.config.EmployeeConfig;
import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.dto.PageResponseDto;
import com.example.imbank.entity.Department;
import com.example.imbank.entity.Employee;
import com.example.imbank.repository.DepartmentRepository;
import com.example.imbank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeConfig employeeConfig;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());

        // default salary if not provided, n
        BigDecimal salary = dto.getSalary() != null ? dto.getSalary() : employeeConfig.getDefaultSalary();

        if (salary.compareTo(employeeConfig.getMinSalary()) < 0) {
            throw new RuntimeException("Salary cannot be less than " + employeeConfig.getMinSalary());
        }
        if (salary.compareTo(employeeConfig.getMaxSalary()) > 0) {
            throw new RuntimeException("Salary cannot exceed " + employeeConfig.getMaxSalary());
        }

        employee.setSalary(salary);
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
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


    @Override
    public List<EmployeeResponseDto> getEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        return employeeRepository.findBySalaryRange(minSalary, maxSalary)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> getEmployeesByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartmentName(departmentName)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> getAboveAverageSalaryEmployees() {
        return employeeRepository.findAboveAverageSalary()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> searchEmployeesByName(String keyword) {
        return employeeRepository.searchByName(keyword)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public PageResponseDto<EmployeeResponseDto> getEmployeesPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        List<EmployeeResponseDto> content = employeePage.getContent()
                .stream()
                .map(this::toResponseDto)
                .toList();

        return new PageResponseDto<>(
                content,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isLast()
        );
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
