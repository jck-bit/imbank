package com.example.imbank.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void handleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Employee", "id", 1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("Employee");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Should handle BadRequestException")
    void handleBadRequestException() {
        // Given
        BadRequestException exception = new BadRequestException("Invalid salary range");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid salary range");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void handleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        FieldError fieldError = new FieldError("employeeDto", "email", "must be a valid email");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input data");
        assertThat(response.getBody().getValidationErrors()).containsKey("email");
        assertThat(response.getBody().getValidationErrors().get("email")).isEqualTo("must be a valid email");
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException")
    void handleDuplicateResourceException() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Employee with email already exists");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResource(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Employee with email already exists");
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException")
    void handleHttpMessageNotReadableException() {
        // Given
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Malformed JSON",
                (org.springframework.http.HttpInputMessage) null
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMalformedJson(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Malformed JSON");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid JSON format. Please check your Request Body.");
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException for email duplicate")
    void handleDataIntegrityViolationException_EmailDuplicate() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "Duplicate entry 'test@example.com' for key 'email'"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("already exists");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void handleGenericException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
