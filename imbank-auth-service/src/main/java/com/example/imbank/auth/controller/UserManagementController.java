package com.example.imbank.auth.controller;
import com.example.imbank.auth.dto.UserResponseDto;
import com.example.imbank.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    /**
     * Grant admin role to a user
     * Only ADMIN can access
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/roles/admin")
    public ResponseEntity<UserResponseDto> grantAdminRole(@PathVariable Long userId) {
        UserResponseDto user = userService.addRoleToUser(userId, "ROLE_ADMIN");
        return ResponseEntity.ok(user);
    }

}
