package com.example.imbank.entity;
import jakarta.persistence.*;
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

        @Column(name = "full_name")
        private String fullName;

        private String phoneNumber;

        @Column(unique = true, nullable = false)
        private String email;
        private BigDecimal salary;

        @ManyToOne
        @JoinColumn(name = "department_id", nullable = false)
        private Department department;

        /// /an empl;oyee can be  USER .. or not

        @OneToOne
        @JoinColumn(name = "user_id", nullable = true)
        private User user;

    }
