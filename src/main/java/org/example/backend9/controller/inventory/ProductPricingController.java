package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductPricingRequest;
import org.example.backend9.dto.response.inventory.ProductPricingResponse;
import org.example.backend9.service.inventory.ProductPricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/productPricings")
@RequiredArgsConstructor
// Thêm bảo mật nếu b cần hạn chế quyền chỉnh sửa giá
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProductPricingController {

    private final ProductPricingService pricingService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<ProductPricingResponse> pricings = pricingService.getAll();
            return ResponseEntity.ok(pricings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách bảng giá: " + e.getMessage());
        }
    }

    @PostMapping("/setup") // Nút "Thiết lập giá" hoặc "Cập nhật giá"
    public ResponseEntity<?> setup(@RequestBody ProductPricingRequest request) {
        try {
            // Hàm này sẽ tự động Create nếu chưa có hoặc Update nếu đã tồn tại giá cho Variant+Store đó
            ProductPricingResponse response = pricingService.setupPrice(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi thiết lập giá: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/approve") // Nút "Duyệt giá" (Chuyển status sang 'Đang áp dụng')
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        try {
            pricingService.approvePrice(id);
            return ResponseEntity.ok("Đã duyệt bảng giá ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi duyệt giá: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}") // Nút "Xóa"
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            String message = pricingService.delete(id);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa bảng giá: " + e.getMessage());
        }
    }
}