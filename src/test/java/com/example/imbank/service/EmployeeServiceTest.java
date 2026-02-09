package com.example.imbank.service;

import com.example.imbank.config.EmployeeConfig;
import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.entity.Department;
import com.example.imbank.entity.Employee;
import com.example.imbank.exception.BadRequestException;
import com.example.imbank.exception.ResourceNotFoundException;
import com.example.imbank.repository.DepartmentRepository;
import com.example.imbank.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeConfig employeeConfig;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private Department department;
    private EmployeeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT");
        department.setDescription("Information Technology");

        employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John Doe");
        employee.setEmail("john.doe@imbank.com");
        employee.setSalary(new BigDecimal("75000"));
        employee.setDepartment(department);

        requestDto = new EmployeeRequestDto();
        requestDto.setFullName("John Doe");
        requestDto.setEmail("john.doe@imbank.com");
        requestDto.setSalary(new BigDecimal("75000"));
        requestDto.setDepartmentId(1L);
    }

    @Test
    @DisplayName("Should Create Employee Successfully")
    void createEmployeeSuccessfully() {
        //given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeConfig.getMinSalary()).thenReturn(new BigDecimal("30000"));
        when(employeeConfig.getMaxSalary()).thenReturn(new BigDecimal("500000"));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        //when
        EmployeeResponseDto result = employeeService.createEmployee(requestDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@imbank.com");
        assertThat(result.getDepartmentName()).isEqualTo("IT");

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should Throw Exception when department not Found")
    void createEmployeeDepartmentNotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        //when/ Then
        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department Not Found");

        verify(employeeRepository, never()).save(any(Employee.class));

    }

    @Test
    @DisplayName("Should thrw exception when salarybelow minimum")
    void createEmployee_SalaryBelowMinimum() {
        // Given
        requestDto.setSalary(new BigDecimal("20000"));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeConfig.getMinSalary()).thenReturn(new BigDecimal("30000"));
        when(employeeConfig.getMaxSalary()).thenReturn(new BigDecimal("500000"));

        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Salary cannot be less than");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should Throw Exception when salary exceeds maximum")
    void createEmployee_SalaryExceedsMaximum() {
        // Given
        requestDto.setSalary(new BigDecimal("600000"));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeConfig.getMinSalary()).thenReturn(new BigDecimal("30000"));
        when(employeeConfig.getMaxSalary()).thenReturn(new BigDecimal("500000"));

        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Salary cannot exceed");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should use Default salary when salary not provided")
    void createEmployee_UseDefaultSalary() {
        // Given
        requestDto.setSalary(null);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeConfig.getDefaultSalary()).thenReturn(new BigDecimal("50000"));
        when(employeeConfig.getMinSalary()).thenReturn(new BigDecimal("30000"));
        when(employeeConfig.getMaxSalary()).thenReturn(new BigDecimal("500000"));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        employeeService.createEmployee(requestDto);

        // Then
        verify(employeeConfig).getDefaultSalary();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should Return all Employees")
    //Given
    void getAllEmployees_Success() {
        // Given
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        // When
        List<EmployeeResponseDto> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return employee by id")
    void getEmployeeById_Success() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // When
        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when employee not found by id")
    void getEmployeeById_NotFound() {
        // Given
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("Should update employee successfully")
    void updateEmployee_Success() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        requestDto.setFullName("John Updated");

        // When
        EmployeeResponseDto result = employeeService.updateEmployee(1L, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void deleteEmployee_Success() {
        // Given
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        // When
        employeeService.deleteEmployee(1L);

        // Then
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent employee")
    void deleteEmployee_NotFound() {
        // Given
        when(employeeRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");

        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
