package com.example.imbank.repository;

import com.example.imbank.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department>findByName(String name);

    // JPQL - Find departments with employees
    @Query("SELECT DISTINCT d FROM Department d WHERE d.id IN (SELECT e.department.id FROM Employee e)")
    List<Department> findDepartmentsWithEmployees();

    // JPQL - Search by name or description
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Department> searchByKeyword(@Param("keyword") String keyword);

}
