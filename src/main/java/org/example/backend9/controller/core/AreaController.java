package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.AreaRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.AreaResponse;
import org.example.backend9.service.core.AreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/core/areas")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    // Chỉ cần đăng nhập
    @GetMapping
    public ResponseEntity<ApiResponse<List<AreaResponse>>> getAllAreas() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy danh sách khu vực thành công", areaService.getAllAreas())
        );
    }

    // ADMIN + SUPER_ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AreaResponse>> createArea(@Valid @RequestBody AreaRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tạo khu vực thành công", areaService.createArea(request))
        );
    }

    // ADMIN + SUPER_ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AreaResponse>> updateArea(
            @PathVariable Integer id,
            @Valid @RequestBody AreaRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cập nhật khu vực thành công", areaService.updateArea(id, request))
        );
    }

    // Chỉ SUPER_ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable Integer id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Khóa khu vực thành công", null)
        );
    }
}