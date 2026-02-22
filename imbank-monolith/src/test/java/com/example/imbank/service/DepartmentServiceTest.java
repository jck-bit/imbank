package com.example.imbank.service;


import com.example.imbank.dto.DepartmentRequestDto;
import com.example.imbank.dto.DepartmentResponseDto;
import com.example.imbank.entity.Department;
import com.example.imbank.exception.ResourceNotFoundException;
import com.example.imbank.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.doNothing;


@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT");
        department.setDescription("Information Technology");

        requestDto = new DepartmentRequestDto();
        requestDto.setName("IT");
        requestDto.setDescription("Information Technology");

        departmentService = new DepartmentServiceImpl(departmentRepository);
    }


    @Test
    @DisplayName("Should create department successfully")
    void createDepartment_Success() {
        // Given
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponseDto result = departmentService.createDepartment(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("IT");
        assertThat(result.getDescription()).isEqualTo("Information Technology");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("Should return all departments")
    void getAllDepartments_Success() {
        // Given
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        // When
        List<DepartmentResponseDto> result = departmentService.getAllDepartments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("IT");
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return department by id")
    void getDepartmentById_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // SELECT * FROM   departments WHERE ID
        // When
        DepartmentResponseDto result = departmentService.getDepartmentById(1L);

        // Then
        assertThat(result).isNotNull(); // //false
        assertThat(result.getName()).isEqualTo("IT");
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when department not found")
    void getDepartmentById_NotFound() {
        // Given
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    @DisplayName("Should update department successfully")
    void updateDepartment_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        requestDto.setName("IT Updated");

        // When
        DepartmentResponseDto result = departmentService.updateDepartment(1L, requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("Should delete department successfully")
    void deleteDepartment_Success() {
        // Given
        when(departmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(departmentRepository).deleteById(1L);
        // When
        departmentService.deleteDepartment(1L);

        // Then
        verify(departmentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent department")
    void deleteDepartment_NotFound() {
        // Given
        when(departmentRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> departmentService.deleteDepartment(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");

        verify(departmentRepository, never()).deleteById(anyLong());
    }
}
