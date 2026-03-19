package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.EmployeeCreateRequest;
import org.example.backend9.dto.response.core.EmployeeResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeManageServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private EmployeeManageService employeeManageService;

    private Employee mockEmp;

    @BeforeEach
    void setUp() {
        mockEmp = new Employee();
        mockEmp.setId(1);
        mockEmp.setFullName("Ngọc IT");
        mockEmp.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    void getAllEmployees_ShouldReturnList() {
        when(employeeRepository.findAll()).thenReturn(List.of(mockEmp));
        List<EmployeeResponse> result = employeeManageService.getAllEmployees();
        assertEquals(1, result.size());
        verify(employeeRepository).findAll();
    }

    @Test
    void createEmployee_ShouldSaveSuccess() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setPassword("123456");

        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(employeeRepository.save(any())).thenReturn(mockEmp);

        EmployeeResponse result = employeeManageService.createEmployee(request);
        assertNotNull(result);
    }

    @Test
    void toggleEmployeeStatus_ShouldSwitchStatus() {
        // Sử dụng any() thay vì anyInt() để khớp với kiểu Integer
        when(employeeRepository.findById(any())).thenReturn(Optional.of(mockEmp));

        employeeManageService.toggleEmployeeStatus(1);

        assertEquals(EntityStatus.INACTIVE, mockEmp.getStatus());
        verify(employeeRepository).save(mockEmp);
    }

    @Test
    void deleteEmployee_ShouldDeleteFromDatabase() {
        // 1. Phải giả lập existsById trả về true thì logic mới chạy tiếp
        when(employeeRepository.existsById(any())).thenReturn(true);

        // 2. Thực thi
        employeeManageService.deleteEmployee(1);

        // 3. Kiểm tra xem hàm deleteById có được gọi không (thay vì kiểm tra status)
        verify(employeeRepository, times(1)).deleteById(1);
    }
}