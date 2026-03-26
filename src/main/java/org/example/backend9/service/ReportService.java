package org.example.backend9.service;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.response.*;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.repository.inventory.CategoryRepository; // Giả định bác có repo này
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public List<RevenueReportResponse> getRevenue(String startDate, String endDate, String period, Long storeId) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        List<Object[]> rawData = orderRepository.getRevenueReportNative(start, end, storeId);

        return rawData.stream().map(row -> new RevenueReportResponse(
                (String) row[0],
                (BigDecimal) row[1],
                (BigDecimal) row[2],
                ((Number) row[3]).longValue()
        )).collect(Collectors.toList());
    }

    public List<ProductReportResponse> getTopProducts(String startDate, String endDate, Integer limit, Long storeId) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 10);
        return orderRepository.getTopSellingProducts(start, end, pageable, storeId);
    }

    public List<StorePerformanceResponse> getStoreComparison(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return orderRepository.getStoreComparisonReport(start, end);
    }

    // 🟢 XỬ LÝ CHO BẢNG HIỆU SUẤT NHÂN VIÊN (Ảnh 59ff0b)
    public List<EmployeePerformanceResponse> getEmployeePerformance(String startDate, String endDate, Long storeId) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        // Giả sử bác đã viết câu query này trong OrderRepository
        List<Object[]> rawData = orderRepository.getEmployeePerformanceNative(start, end, storeId);

        return rawData.stream().map(row -> {
            BigDecimal revenue = (BigDecimal) row[3];
            BigDecimal kpi = new BigDecimal("10000000"); // Ví dụ KPI cứng 50 triệu, bác có thể lấy từ DB

            // Tính tỉ lệ hoàn thành
            double rate = 0;
            if (kpi.compareTo(BigDecimal.ZERO) > 0) {
                rate = revenue.multiply(new BigDecimal("100"))
                        .divide(kpi, 2, RoundingMode.HALF_UP).doubleValue();
            }

            return EmployeePerformanceResponse.builder()
                    .employeeCode((String) row[0])
                    .fullName((String) row[1])
                    .storeName((String) row[2])
                    .totalRevenue(revenue)
                    .orderCount(((Number) row[4]).longValue())
                    .kpiAmount(kpi)
                    .completionRate(rate)
                    .rank(rate >= 100 ? "Xuất sắc" : (rate >= 80 ? "Khá" : "Cần cố gắng"))
                    .build();
        }).collect(Collectors.toList());
    }

    // 🟢 XỬ LÝ CHO BIỂU ĐỒ CƠ CẤU DANH MỤC (Ảnh 59feea)
    public List<CategoryRatioResponse> getCategoryRatio() {
        List<Object[]> rawData = categoryRepository.getCategoryRatioNative();
        return rawData.stream().map(row -> new CategoryRatioResponse(
                (String) row[0],             // categoryName
                ((Number) row[1]).longValue(), // productCount
                0.0                          // percentage (tạm thời)
        )).collect(Collectors.toList());
    }
}