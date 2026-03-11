package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.StoreRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.StoreResponse;
import org.example.backend9.service.core.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // ADMIN + SUPER_ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores() {
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Lấy danh sách cửa hàng thành công",
                        storeService.getAllStores())
        );
    }

    // ADMIN + SUPER_ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody StoreRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Thêm mới cửa hàng thành công",
                        storeService.createStore(request))
        );
    }

    // ADMIN + SUPER_ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Integer id,
            @Valid @RequestBody StoreRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Cập nhật cửa hàng thành công",
                        storeService.updateStore(id, request))
        );
    }
}