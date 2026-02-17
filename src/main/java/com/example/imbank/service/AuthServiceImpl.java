package com.example.imbank.service;

import com.example.imbank.dto.*;
import com.example.imbank.entity.RefreshToken;
import com.example.imbank.entity.Role;
import com.example.imbank.entity.User;
import com.example.imbank.exception.DuplicateResourceException;
import com.example.imbank.exception.InvalidCredentialsException;
import com.example.imbank.exception.ResourceNotFoundException;
import com.example.imbank.exception.TokenExpiredException;
import com.example.imbank.repository.RefreshTokenRepository;
import com.example.imbank.repository.RoleRepository;
import com.example.imbank.repository.UserRepository;
import com.example.imbank.security.CustomUserDetails;
import com.example.imbank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Login attempt for user: {}", dto.getUsernameOrEmail());

        // 1. Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsernameOrEmail(),
                        dto.getPassword()
                )
        );

        // 2. Set authentication in context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Get user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 4. Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        // 5. Build response
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)  // 15 minutes =in secs
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        log.info("Registration attempt for username: {}", dto.getUsername());

        // 1. Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }

        // 2. Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        // 3. Create new user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // Hash password
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        // 4. Assign default role (ROLE_USER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // 5. Save user
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        // 6. Convert to response DTO
        return convertToUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDto refreshToken(RefreshTokenRequestDto dto) {
        log.info("Token refresh attempt");

        // we find the resfrsh token in db
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        /// we Check if token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token has expired");
        }

        // we  Check if token is revoked////
        if (refreshToken.getRevoked()) {
            throw new TokenExpiredException("Refresh token has been revoked");
        }

        //  Get user and generate new access token
        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        log.info("Token refreshed for user: {}", user.getUsername());

        //  token (keep same refresh token)
        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(dto.getRefreshToken())  // Same refresh token
                .tokenType("Bearer")
                .expiresIn(900L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void logout(String username) {
        log.info("Logout  for user: {}", username);

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Revoke all refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());

        log.info("User logged out successfully: {}", username);
    }

    @Override
    public UserResponseDto getCurrentUser() {
        // Get authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("No authenticated user found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return convertToUserResponseDto(user);
    }

    // Helper method: Create refresh token
    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));  // 7 days
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    // Helper method: Convert User to UserResponseDto
    private UserResponseDto convertToUserResponseDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .accountNonLocked(user.getAccountNonLocked())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}