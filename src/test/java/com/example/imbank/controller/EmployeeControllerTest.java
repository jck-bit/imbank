package com.example.imbank.controller;


import com.example.imbank.dto.EmployeeRequestDto;
import com.example.imbank.dto.EmployeeResponseDto;
import com.example.imbank.service.EmployeeService;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import java.math.BigDecimal;
import java.util.List;

import com.example.imbank.exception.ResourceNotFoundException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc; ///mock http requests

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private EmployeeService employeeService; ///fake service

    private EmployeeRequestDto requestDto;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setup() {
        requestDto = new EmployeeRequestDto();
        requestDto.setFullName("John Doe");
        requestDto.setEmail("john.doe@imbank.com");
        requestDto.setSalary(new BigDecimal("75000"));
        requestDto.setDepartmentId(1L);

        responseDto = new EmployeeResponseDto(1L, "John Doe", "john.doe@imbank.com", "IT");
    }

    @Test
    @DisplayName("POST /api/employees - Should create employee")
    void createEmployee_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@imbank.com"))
                .andExpect(jsonPath("$.departmentName").value("IT"));
    }

    @Test
    @DisplayName("POST /api/employees - Should return 400 for invalid input")
    void createEmployee_ValidationError() throws Exception {
        EmployeeRequestDto invalidDto = new EmployeeRequestDto();
        invalidDto.setFullName(""); // Invalid - empty name
        invalidDto.setEmail("invalid-email"); // Invalid email format

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/employees - Should return all employees")
    void getAllEmployees_Success() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Should return employee by id")
    void getEmployeeById_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Should return 404 for non-existent employee")
    void getEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Should update employee")
    void updateEmployee_Success() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Should delete employee")
    void deleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Should return 404 for non-existent employee")
    void deleteEmployee_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee", "id", 999L))
                .when(employeeService).deleteEmployee(999L);

        mockMvc.perform(delete("/api/employees/999"))
                .andExpect(status().isNotFound());
    }
}
