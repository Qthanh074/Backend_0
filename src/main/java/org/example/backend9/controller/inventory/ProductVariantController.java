package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.VariantRequest;
import org.example.backend9.dto.response.inventory.VariantDetailResponse;
import org.example.backend9.service.inventory.ProductVariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/productVariants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    // ==========================================
    // 1. LẤY DANH SÁCH BIẾN THỂ THEO SẢN PHẨM GỐC
    // ==========================================
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getVariantsByProduct(@PathVariable Long productId) {
        try {
            List<VariantDetailResponse> variants = variantService.getVariantsByProductId(productId);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách biến thể: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. LẤY CHI TIẾT 1 BIẾN THỂ THEO ID
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariantById(@PathVariable Long id) {
        try {
            VariantDetailResponse variant = variantService.getVariantById(id);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. TẠO MỚI 1 BIẾN THỂ
    // ==========================================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createVariant(@RequestBody VariantRequest request) {
        try {
            if (request.getProductId() == null) {
                return ResponseEntity.badRequest().body("Lỗi: productId (ID Sản phẩm gốc) không được để trống!");
            }
            VariantDetailResponse variant = variantService.createVariant(request);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo biến thể: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. CẬP NHẬT BIẾN THỂ (Sửa giá, tồn kho, trạng thái...)
    // ==========================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateVariant(@PathVariable Long id, @RequestBody VariantRequest request) {
        try {
            VariantDetailResponse variant = variantService.updateVariant(id, request);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật biến thể: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. XÓA BIẾN THỂ
    // ==========================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteVariant(@PathVariable Long id) {
        try {
            variantService.deleteVariant(id);
            return ResponseEntity.ok("Đã xóa thành công biến thể ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa biến thể: " + e.getMessage());
        }
    }
}