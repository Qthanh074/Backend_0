package org.example.backend9.controller.sales;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.sales.PromotionRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.sales.PromotionResponse;
import org.example.backend9.service.sales.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.example.backend9.dto.response.sales.PromotionCheckResponse;
import org.example.backend9.dto.request.sales.PromotionCheckRequest;
import java.util.List;

@RestController
@RequestMapping("/api/sales/promotions")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách KM thành công", promotionService.getAllPromotions()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tạo KM thành công", promotionService.createPromotion(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(@PathVariable Integer id, @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật KM thành công", promotionService.updatePromotion(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa KM thành công", null));
    }
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<PromotionCheckResponse>> check(@RequestBody PromotionCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Check hoàn tất", promotionService.validatePromotion(request)));
    }
}