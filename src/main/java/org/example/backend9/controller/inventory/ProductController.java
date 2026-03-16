package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.service.inventory.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
// Áp dụng bảo mật cho toàn bộ Controller
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            // Trả về danh sách sản phẩm kèm theo tất cả các biến thể và giá
            List<ProductResponse> products = productService.getAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            // Xử lý tạo sản phẩm, tự động gọi Service Variant để lưu biến thể
            ProductResponse response = productService.create(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Trả về lỗi cụ thể (VD: Trùng mã sản phẩm, trùng SKU biến thể)
            return ResponseEntity.badRequest().body("Lỗi tạo sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            // Cập nhật thông tin chung và đồng bộ lại danh sách biến thể thông qua Service
            ProductResponse response = productService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            // Xóa sản phẩm và các biến thể liên quan (Cascade)
            String message = productService.delete(id);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}