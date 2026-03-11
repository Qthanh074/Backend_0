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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        // Trả về danh sách sản phẩm kèm theo tất cả các biến thể (Color, Size, Unit)
        return ResponseEntity.ok(productService.getAll());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        // Xử lý tạo sản phẩm và lặp qua danh sách variants để lưu
        return ResponseEntity.ok(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        // Cập nhật thông tin chung và đồng bộ lại danh sách biến thể
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        // Trả về chuỗi thông báo "Đã xóa thành công sản phẩm: ..."
        return ResponseEntity.ok(productService.delete(id));
    }
}