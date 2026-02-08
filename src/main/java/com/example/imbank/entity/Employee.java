package com.example.imbank.entity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
    @Table(name = "employees")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor

    public class Employee extends BaseEntity {
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
