package org.example.backend9.controller.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository; // 🟢 THÊM IMPORT NÀY
import org.example.backend9.service.sales.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.example.backend9.dto.response.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository; // 🟢 INJECT THÊM STORE REPOSITORY

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CASHIER')")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Lấy nhân viên từ Security Context
        Employee employee = employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        // 🟢 SỬA LẠI LOGIC LẤY CỬA HÀNG 🟢
        // Ưu tiên 1: Lấy từ màn hình POS Frontend truyền xuống
        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId()).orElse(null);
        }
        // Ưu tiên 2: Nếu FE không gửi (đơn online), lấy cửa hàng mặc định của nhân viên
        if (store == null) {
            store = employee.getStore();
        }

        return ResponseEntity.ok(orderService.createOrder(request, employee, store));
    }

    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công",
                orderService.getOrdersByFilter(channel, status, type)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> statusRequest) {
        OrderStatus newStatus = OrderStatus.valueOf(statusRequest.get("status").toUpperCase());
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật thành công",
                orderService.updateStatus(id, newStatus)));
    }
}