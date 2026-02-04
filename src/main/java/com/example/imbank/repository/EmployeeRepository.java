package com.example.imbank.repository;

import com.example.imbank.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment_Id(Long departmentId);
}
