package org.example.backend9.controller.sales;

import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.sales.CustomerResponse;
import org.example.backend9.entity.sales.Loyalty;
import org.example.backend9.repository.sales.LoyaltyRepository;
import org.example.backend9.service.sales.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sales/loyalty")
public class LoyaltyController {

    private final CustomerService customerService;
    private final LoyaltyRepository configRepository; // Thành nhớ tạo Repo này nhé

    public LoyaltyController(CustomerService customerService, LoyaltyRepository configRepository) {
        this.customerService = customerService;
        this.configRepository = configRepository;
    }

    // 1. API Lấy danh sách hội viên cho bảng
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getMembers(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", customerService.getLoyaltyMembers(search)));
    }

    // 2. API Lấy cấu hình hiện tại (đổ vào 2 ô nhập liệu phía trên)
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Loyalty>> getConfig() {
        Loyalty config = configRepository.findById(1)
                .orElse(new Loyalty(1, new BigDecimal("100000"), new BigDecimal("100")));
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", config));
    }

    // 3. API Lưu cấu hình (khi nhấn nút "Lưu cấu hình" màu xanh)
    @PostMapping("/config")
    public ResponseEntity<ApiResponse<Loyalty>> saveConfig(@RequestBody Loyalty newConfig) {
        newConfig.setId(1); // Luôn đảm bảo chỉ sửa dòng số 1
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã lưu cấu hình tích điểm", configRepository.save(newConfig)));
    }
}