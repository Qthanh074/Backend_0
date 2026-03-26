package org.example.backend9.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    // 1. Báo cáo doanh thu (Biểu đồ đường/cột)
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "day") String period,
            @RequestParam(required = false) Long storeId) {

        var data = reportService.getRevenue(startDate, endDate, period, storeId);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy dữ liệu doanh thu thành công", data));
    }

    // 2. Top sản phẩm bán chạy
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse> getTopProducts(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long storeId) {

        var data = reportService.getTopProducts(startDate, endDate, limit, storeId);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm thành công", data));
    }

    // 3. So sánh hiệu suất giữa các chi nhánh
    @GetMapping("/store-comparison")
    public ResponseEntity<ApiResponse> getStoreComparison(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        var data = reportService.getStoreComparison(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy dữ liệu so sánh chi nhánh thành công", data));
    }

    // 🟢 4. BỔ SUNG CHO ẢNH image_59ff0b.png: Hiệu suất bán hàng nhân viên
    @GetMapping("/employee-performance")
    public ResponseEntity<ApiResponse> getEmployeePerformance(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long storeId) {

        var data = reportService.getEmployeePerformance(startDate, endDate, storeId);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy dữ liệu hiệu suất nhân viên thành công", data));
    }

    // 🟢 5. BỔ SUNG CHO ẢNH image_59feea.png: Tỷ lệ danh mục (Biểu đồ tròn)
    @GetMapping("/category-ratio")
    public ResponseEntity<ApiResponse> getCategoryRatio() {

        var data = reportService.getCategoryRatio();
        return ResponseEntity.ok(new ApiResponse(true, "Lấy tỷ lệ danh mục thành công", data));
    }
}