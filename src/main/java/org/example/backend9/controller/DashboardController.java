package org.example.backend9.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.DashboardFilterRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.RevenueTrendDTO;
import org.example.backend9.dto.response.StoreRevenueDTO;
import org.example.backend9.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getSummary(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long storeId) {

        var data = dashboardService.getSummary(startDate, endDate, storeId);

        // 🟢 SỬA TẠI ĐÂY: Dùng new ApiResponse(...)
        return ResponseEntity.ok(new ApiResponse(true, "Lấy dữ liệu Dashboard thành công", data));
    }
    @PostMapping("/stores-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<StoreRevenueDTO>>> getStorePerformance(
            @Valid @RequestBody DashboardFilterRequest request) {

        List<StoreRevenueDTO> data = dashboardService.getStorePerformanceReport(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Truy xuất báo cáo doanh số cửa hàng thành công", data)
        );
    }

    /**
     * API: Lấy xu hướng biến động doanh thu theo tháng của một cửa hàng.
     */
    @PostMapping("/store-trend/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<RevenueTrendDTO>>> getStoreTrend(
            @PathVariable Integer storeId,
            @Valid @RequestBody DashboardFilterRequest request) {

        List<RevenueTrendDTO> data = dashboardService.getStoreRevenueTrend(storeId, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Truy xuất xu hướng doanh thu thành công", data)
        );
    }
}