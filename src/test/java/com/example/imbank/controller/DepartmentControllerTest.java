package com.example.imbank.controller;


import com.example.imbank.dto.DepartmentRequestDto;
import com.example.imbank.dto.DepartmentResponseDto;
import com.example.imbank.service.DepartmentService;
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
import static org.mockito.ArgumentMatchers.eq;
import java.util.List;

import com.example.imbank.exception.ResourceNotFoundException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private DepartmentService departmentService;

    private DepartmentRequestDto requestDto;
    private DepartmentResponseDto responseDto;

    @BeforeEach
    void setup() {
        requestDto = new DepartmentRequestDto();
        requestDto.setName("IT");
        requestDto.setDescription("Information Technology");

        responseDto = new DepartmentResponseDto(1L, "IT", "Information Technology");
    }

    @Test
    @DisplayName("POST /api/departments - Should create department")
    void createDepartment_Success() throws Exception {
        when(departmentService.createDepartment(any(DepartmentRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.description").value("Information Technology"));
    }

    @Test
    @DisplayName("POST /api/departments - Should return 400 for invalid input")
    void createDepartment_ValidationError() throws Exception {
        DepartmentRequestDto invalidDto = new DepartmentRequestDto();
        invalidDto.setName(""); // Invalid - empty name

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/departments - Should return all departments")
    void getAllDepartments_Success() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("IT"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} - Should return department by id")
    void getDepartmentById_Success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} - Should return 404 for non-existent department")
    void getDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(999L))
                .thenThrow(new ResourceNotFoundException("Department", "id", 999L));

        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/departments/{id} - Should update department")
    void updateDepartment_Success() throws Exception {
        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT"));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} - Should delete department")
    void deleteDepartment_Success() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} - Should return 404 for non-existent department")
    void deleteDepartment_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Department", "id", 999L))
                .when(departmentService).deleteDepartment(999L);

        mockMvc.perform(delete("/api/departments/999"))
                .andExpect(status().isNotFound());
    }
}
