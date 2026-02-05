package com.example.imbank.repository;

import com.example.imbank.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.math.BigDecimal;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment_Id(Long departmentId);


    // JPQL - Find employees by salary range
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
    List<Employee> findBySalaryRange(@Param("minSalary") BigDecimal minSalary,
                                     @Param("maxSalary") BigDecimal maxSalary);

    // JPQL - Find employees by department name (join)
    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);

    // JPQL - Find employees with salary greater than average
    @Query("SELECT e FROM Employee e WHERE e.salary > (SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findAboveAverageSalary();

    // JPQL - Search by name
    @Query("SELECT e FROM Employee e WHERE LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByName(@Param("keyword") String keyword);
}
