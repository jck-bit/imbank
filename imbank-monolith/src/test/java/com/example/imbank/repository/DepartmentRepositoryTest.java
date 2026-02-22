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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department itDepartment;
    private Department hrDepartment;
    private Department financeDepartment;

    @BeforeEach
    void setUp() {
        // Create departments
        itDepartment = new Department();
        itDepartment.setName("IT");
        itDepartment.setDescription("Information Technology Department");
        itDepartment = departmentRepository.save(itDepartment);

        hrDepartment = new Department();
        hrDepartment.setName("HR");
        hrDepartment.setDescription("Human Resources Department");
        hrDepartment = departmentRepository.save(hrDepartment);

        financeDepartment = new Department();
        financeDepartment.setName("Finance");
        financeDepartment.setDescription("Finance and Accounting Department");
        financeDepartment = departmentRepository.save(financeDepartment);

        // Add employees to IT department
        Employee emp1 = new Employee();
        emp1.setFullName("John Doe");
        emp1.setEmail("john.doe@imbank.com");
        emp1.setSalary(new BigDecimal("75000"));
        emp1.setDepartment(itDepartment);
        employeeRepository.save(emp1);

        // Add employees to HR department
        Employee emp2 = new Employee();
        emp2.setFullName("Jane Smith");
        emp2.setEmail("jane.smith@imbank.com");
        emp2.setSalary(new BigDecimal("65000"));
        emp2.setDepartment(hrDepartment);
        employeeRepository.save(emp2);

        // Finance department has no employees
    }

    @Test
    @DisplayName("Should find department by name")
    void findByName() {
        // When
        Optional<Department> result = departmentRepository.findByName("IT");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("IT");
        assertThat(result.get().getDescription()).isEqualTo("Information Technology Department");
    }

    @Test
    @DisplayName("Should return empty when department name not found")
    void findByName_NotFound() {
        // When
        Optional<Department> result = departmentRepository.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find departments with employees")
    void findDepartmentsWithEmployees() {
        // When
        List<Department> departments = departmentRepository.findDepartmentsWithEmployees();

        // Then
        assertThat(departments).hasSize(2);
        assertThat(departments).extracting(Department::getName)
                .containsExactlyInAnyOrder("IT", "HR");
        assertThat(departments).extracting(Department::getName)
                .doesNotContain("Finance");
    }

    @Test
    @DisplayName("Should search departments by keyword in name")
    void searchByKeyword_Name() {
        // When
        List<Department> departments = departmentRepository.searchByKeyword("finance");

        // Then
        assertThat(departments).hasSize(1);
        assertThat(departments.get(0).getName()).isEqualTo("Finance");
    }

    @Test
    @DisplayName("Should search departments by keyword in description")
    void searchByKeyword_Description() {
        // When
        List<Department> departments = departmentRepository.searchByKeyword("technology");

        // Then
        assertThat(departments).hasSize(1);
        assertThat(departments.get(0).getName()).isEqualTo("IT");
    }

    @Test
    @DisplayName("Should search departments case insensitively")
    void searchByKeyword_CaseInsensitive() {
        // When
        List<Department> departments = departmentRepository.searchByKeyword("HUMAN");

        // Then
        assertThat(departments).hasSize(1);
        assertThat(departments.get(0).getName()).isEqualTo("HR");
    }

    @Test
    @DisplayName("Should return multiple departments matching keyword")
    void searchByKeyword_MultipleMatches() {
        // When
        List<Department> departments = departmentRepository.searchByKeyword("department");

        // Then
        assertThat(departments).hasSize(3);
        assertThat(departments).extracting(Department::getName)
                .containsExactlyInAnyOrder("IT", "HR", "Finance");
    }

    @Test
    @DisplayName("Should return empty list when no departments match keyword")
    void searchByKeyword_NoMatch() {
        // When
        List<Department> departments = departmentRepository.searchByKeyword("Marketing");

        // Then
        assertThat(departments).isEmpty();
    }
}
