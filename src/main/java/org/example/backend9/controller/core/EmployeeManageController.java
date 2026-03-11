package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.EmployeeUpdateRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.EmployeeResponse;
import org.example.backend9.service.core.EmployeeManageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/core/employees")
public class EmployeeManageController {

    private final EmployeeManageService employeeManageService;

    public EmployeeManageController(EmployeeManageService employeeManageService) {
        this.employeeManageService = employeeManageService;
    }

    // Chỉ cần đăng nhập
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy danh sách nhân viên thành công",
                        employeeManageService.getAllEmployees())
        );
    }

    // ADMIN + SUPER_ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cập nhật thông tin nhân viên thành công",
                        employeeManageService.updateEmployee(id, request))
        );
    }

    // Chỉ SUPER_ADMIN
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Integer id) {
        employeeManageService.toggleEmployeeStatus(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Đổi trạng thái tài khoản thành công", null)
        );
    }
}