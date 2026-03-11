package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.SupplierRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.SupplierResponse;
import org.example.backend9.service.core.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // Xem danh sách
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Lấy danh sách nhà cung cấp thành công",
                        supplierService.getAllSuppliers())
        );
    }

    // Thêm mới
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Thêm mới nhà cung cấp thành công",
                        supplierService.createSupplier(request))
        );
    }

    // Cập nhật
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Integer id,
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Cập nhật nhà cung cấp thành công",
                        supplierService.updateSupplier(id, request))
        );
    }

    // Khóa (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Đã khóa nhà cung cấp thành công",
                        null)
        );
    }
}