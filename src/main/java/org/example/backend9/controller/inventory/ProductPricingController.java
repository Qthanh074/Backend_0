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
    // 🟢 1. API DUYỆT HÀNG LOẠT (Nút "Duyệt Giá Mới")
    @PostMapping("/bulk-approve")
    public ResponseEntity<?> bulkApprove(@RequestBody List<Integer> ids) {
        try {
            ids.forEach(pricingService::approvePrice);
            return ResponseEntity.ok("Đã duyệt thành công " + ids.size() + " bảng giá");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi duyệt hàng loạt: " + e.getMessage());
        }
    }

    // 🟢 2. API IMPORT EXCEL (Nút "Import Giá")
    @PostMapping("/import")
    public ResponseEntity<?> importPricings(@RequestBody List<ProductPricingRequest> requests) {
        try {
            // Hàm này bác có thể viết lặp qua list requests và gọi hàm setupPrice
            requests.forEach(pricingService::setupPrice);
            return ResponseEntity.ok("Import thành công " + requests.size() + " dòng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi import: " + e.getMessage());
        }
    }

    // 🟢 3. API TÌM KIẾM THEO TÊN/MÃ (Ô "Tìm mã hàng/Tên hàng...")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query) {
        // Bác có thể viết thêm logic filter trong Service
        return ResponseEntity.ok(pricingService.search(query));
    }
}