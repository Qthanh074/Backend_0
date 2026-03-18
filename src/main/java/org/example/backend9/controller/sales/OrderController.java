package org.example.backend9.controller.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.service.sales.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.backend9.dto.response.ApiResponse;
@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CASHIER')") // Cho phép cả thu ngân tạo đơn
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {

        // TODO: Trong thực tế, b sẽ lấy thông tin Employee (nhân viên bán hàng) và Store (cửa hàng)
        // từ token JWT của người đang đăng nhập.
        // Hiện tại để test API nhanh, mình sẽ truyền null vào, trong Service đã xử lý an toàn.

        OrderResponse response = orderService.createOrder(request, null, null);

        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) { // type để phân biệt Lịch sử

        // TODO: Gọi Service để filter danh sách dựa trên channel/status
        // List<OrderResponse> orders = orderService.getOrdersByFilter(channel, status, type);

        // Return dummy data hoặc danh sách rỗng để React không lỗi
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách thành công", java.util.Collections.emptyList()));
    }
}