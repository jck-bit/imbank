package com.example.imbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Set<String> roles;  // s: ["ROLE_USER", "ROLE_ADMIN"]
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}