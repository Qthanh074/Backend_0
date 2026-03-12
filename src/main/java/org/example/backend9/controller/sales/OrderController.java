package org.example.backend9.controller.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.service.sales.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails currentUser // Lấy user từ Token
    ) {
        // Tìm thông tin nhân viên đang đăng nhập
        Employee employee = employeeRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên đăng nhập!"));

        if (employee.getStore() == null) {
            throw new RuntimeException("Nhân viên chưa được gán chi nhánh (Store)!");
        }

        // Thực hiện thanh toán
        OrderResponse response = orderService.createOrder(request, employee, employee.getStore());

        return ResponseEntity.ok(new ApiResponse<>(true, "Thanh toán và lưu Google Sheet thành công!", response));
    }
}