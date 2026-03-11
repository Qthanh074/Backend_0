package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.RolePermissionRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.RolePermissionResponse;
import org.example.backend9.service.core.RolePermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/role-permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    // ADMIN + SUPER_ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Lấy danh sách phân quyền thành công",
                        rolePermissionService.getAllRoles())
        );
    }

    // Chỉ SUPER_ADMIN được sửa quyền
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> saveRolePermission(
            @Valid @RequestBody RolePermissionRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Cập nhật phân quyền thành công",
                        rolePermissionService.saveOrUpdateRole(request))
        );
    }
}