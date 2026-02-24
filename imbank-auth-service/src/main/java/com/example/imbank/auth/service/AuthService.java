package com.example.imbank.auth.service;

import com.example.imbank.auth.dto.*;

public interface AuthService {

    /**
     * Authenticate user and return JWT tokens
     */
    LoginResponseDto login(LoginRequestDto dto);

    /**
     * Register a new new user account
     */
    UserResponseDto register(RegisterRequestDto dto);

    /**
     * Refresh access token using refresh token
     */
    LoginResponseDto refreshToken(RefreshTokenRequestDto dto);

    /**
     * Logout user (revoke refresh tokens)
     */
    void logout(String username);

    /**
     * Get current authenticated user
     */
    UserResponseDto getCurrentUser();
}