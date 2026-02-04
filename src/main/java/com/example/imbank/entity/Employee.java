package com.example.imbank.entity;


import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;
import java.math.BigDecimal;


    @Entity
    @Table(name = "employees")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor

    public class Employee {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        private String fullName;

        @Column(unique = true, nullable = false)
        private String email;

        private BigDecimal salary;

        @ManyToOne
        @JoinColumn(name = "department_id", nullable = false)
        private Department department;
    }
