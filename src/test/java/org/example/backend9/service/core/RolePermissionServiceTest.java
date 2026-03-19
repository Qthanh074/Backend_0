package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.RolePermissionRequest;
import org.example.backend9.entity.core.RolePermission;
import org.example.backend9.enums.UserRole;
import org.example.backend9.repository.core.RolePermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {
    @Mock private RolePermissionRepository rolePermissionRepository;
    @InjectMocks private RolePermissionService rolePermissionService;

    @Test
    void getAllRoles_ShouldReturnList() {
        RolePermission role = new RolePermission();
        role.setRole(String.valueOf(UserRole.ADMIN)); // Giả sử entity dùng Enum
        when(rolePermissionRepository.findAll()).thenReturn(List.of(role));

        var result = rolePermissionService.getAllRoles();

        // Fix lỗi so sánh: Chuyển Actual về String hoặc so sánh trực tiếp Enum tùy theo DTO của bạn
        assertEquals("ADMIN", result.get(0).getRole().toString());
    }

    @Test
    void saveOrUpdateRole_ShouldReturnResponse() {
        RolePermissionRequest request = new RolePermissionRequest();
        request.setRole("ADMIN"); // Phải set giá trị để tránh lỗi toUpperCase()

        RolePermission role = new RolePermission();
        when(rolePermissionRepository.save(any())).thenReturn(role);

        var result = rolePermissionService.saveOrUpdateRole(request);
        assertNotNull(result);
    }
}