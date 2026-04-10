package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.service.inventory.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProductController {

    private final ProductService productService;

    // 🟢 CHỈ GIỮ LẠI MỘT HÀM GET DUY NHẤT Ở ĐÂY
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Long supplierId) {
        try {
            List<ProductResponse> products;

            // Nếu truyền ?supplierId=... thì lọc, không thì lấy tất cả
            if (supplierId != null) {
                products = productService.getBySupplierId(supplierId);
            } else {
                products = productService.getAll();
            }

            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.create(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            String message = productService.delete(id);
            return ResponseEntity.ok(message);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("KHÔNG THỂ XÓA! Sản phẩm đã có giao dịch.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}