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
import com.example.imbank.exception.ResourceNotFoundException;
import com.example.imbank.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeConfig employeeConfig;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        log.info("Creating employee with email: {}", dto.getEmail());

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Department not found with id: {}", dto.getDepartmentId());
                    return new ResourceNotFoundException("Department", "id", dto.getDepartmentId());
                });

        BigDecimal salary = dto.getSalary() != null ? dto.getSalary() : employeeConfig.getDefaultSalary();
        log.debug("Using salary: {} (default: {})", salary, dto.getSalary() == null);

        if (salary.compareTo(employeeConfig.getMinSalary()) < 0) {
            log.warn("Salary {} is below minimum {}", salary, employeeConfig.getMinSalary());
            throw new BadRequestException("Salary cannot be less than " + employeeConfig.getMinSalary());
        }
        if (salary.compareTo(employeeConfig.getMaxSalary()) > 0) {
            log.warn("Salary {} exceeds maximum {}", salary, employeeConfig.getMaxSalary());
            throw new BadRequestException("Salary cannot exceed " + employeeConfig.getMaxSalary());
        }

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(salary);
        employee.setDepartment(department);

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully with id: {}", saved.getId());

        return toResponseDto(saved);
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees(){
        log.debug("Getting all employees");
        List<Employee> employees = employeeRepository.findAll();
        log.debug("Found {} employees", employees.size());

                return employees.stream()
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
    public EmployeeResponseDto getEmployeeById(Long id) {
        log.debug("Fetching employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee Not found With id: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        return toResponseDto(employee);
    }

    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        log.info("Updating employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee Not found with id: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Department Not found with id: {}", dto.getDepartmentId());
                    return new ResourceNotFoundException("Department", "id", dto.getDepartmentId());
                });

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setDepartment(department);

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully with id: {}", updated.getId());

        return toResponseDto(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);

        if (!employeeRepository.existsById(id)) {
            log.error("Employee not found with id: {}", id);
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with id: {}", id);
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
