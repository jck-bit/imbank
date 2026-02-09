package com.example.imbank.repository;


import com.example.imbank.entity.Department;
import com.example.imbank.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;

    @BeforeEach
    void setUp() {
        // Create departments
        itDepartment = new Department();
        itDepartment.setName("IT");
        itDepartment.setDescription("Information Technology");
        itDepartment = departmentRepository.save(itDepartment);

        hrDepartment = new Department();
        hrDepartment.setName("HR");
        hrDepartment.setDescription("Human Resources");
        hrDepartment = departmentRepository.save(hrDepartment);

        // Create employees
        Employee emp1 = new Employee();
        emp1.setFullName("John Doe");
        emp1.setEmail("john.doe@imbank.com");
        emp1.setSalary(new BigDecimal("75000"));
        emp1.setDepartment(itDepartment);
        employeeRepository.save(emp1);

        Employee emp2 = new Employee();
        emp2.setFullName("Jane Smith");
        emp2.setEmail("jane.smith@imbank.com");
        emp2.setSalary(new BigDecimal("85000"));
        emp2.setDepartment(itDepartment);
        employeeRepository.save(emp2);

        Employee emp3 = new Employee();
        emp3.setFullName("Bob Johnson");
        emp3.setEmail("bob.johnson@imbank.com");
        emp3.setSalary(new BigDecimal("55000"));
        emp3.setDepartment(hrDepartment);
        employeeRepository.save(emp3);
    }

    @Test
    @DisplayName("Should find employees by department ID")
    void findByDepartmentId() {
        // When
        List<Employee> employees = employeeRepository.findByDepartment_Id(itDepartment.getId());

        // Then
        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("Should find employees by salary range")
    void findBySalaryRange() {
        // When
        List<Employee> employees = employeeRepository.findBySalaryRange(
                new BigDecimal("70000"),
                new BigDecimal("90000")
        );

        // Then
        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("Should find employees by department name")
    void findByDepartmentName() {
        // When
        List<Employee> employees = employeeRepository.findByDepartmentName("IT");

        // Then
        assertThat(employees).hasSize(2);
        assertThat(employees).allMatch(e -> e.getDepartment().getName().equals("IT"));
    }

    @Test
    @DisplayName("Should find employees with above average salary")
    void findAboveAverageSalary() {
        // When
        List<Employee> employees = employeeRepository.findAboveAverageSalary();

        // Then
        // Average is (75000 + 85000 + 55000) / 3 = 71666.67
        // So Jane Smith (85000) and John Doe (75000) should be returned
        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("Should search employees by name keyword")
    void searchByName() {
        // When
        List<Employee> employees = employeeRepository.searchByName("john");

        // Then
        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
    }

    @Test
    @DisplayName("Should return empty list when no employees match salary range")
    void findBySalaryRange_NoMatch() {
        // When
        List<Employee> employees = employeeRepository.findBySalaryRange(
                new BigDecimal("100000"),
                new BigDecimal("150000")
        );

        // Then
        assertThat(employees).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when searching for non-existent name")
    void searchByName_NoMatch() {
        // When
        List<Employee> employees = employeeRepository.searchByName("NonExistent");

        // Then
        assertThat(employees).isEmpty();
    }
}
