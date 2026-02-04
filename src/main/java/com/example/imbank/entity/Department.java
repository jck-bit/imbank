package com.example.imbank.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

    @Entity
    @Table(name = "department")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor

    public class Department {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String name;

        private String description;

    }
