package org.example.backend9.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend9.dto.request.DashboardFilterRequest;
import org.example.backend9.dto.response.DashboardSummaryResponse;
import org.example.backend9.dto.response.EmployeePerformanceResponse;
import org.example.backend9.dto.response.RevenueTrendDTO;
import org.example.backend9.dto.response.StoreRevenueDTO;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.repository.OrderAnalyticsRepository;
import org.example.backend9.repository.inventory.ProductRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final OrderAnalyticsRepository orderAnalyticsRepository;

    // =========================================================================
    // 1. HÀM DÀNH CHO GIAO DIỆN DASHBOARD (4 Ô TỔNG, GIAO DỊCH GẦN ĐÂY)
    // =========================================================================
    public DashboardSummaryResponse getSummary(String startDate, String endDate, Long storeId) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        LocalDateTime lastMonthStart = start.minusMonths(1);
        LocalDateTime lastMonthEnd = end.minusMonths(1);

        // Lấy số liệu tổng quát
        BigDecimal currentRevenue = orderRepository.getTotalRevenue(start, end, storeId);
        if (currentRevenue == null) currentRevenue = BigDecimal.ZERO;

        Long currentOrdersCount = orderRepository.getTotalOrders(start, end, storeId);
        if (currentOrdersCount == null) currentOrdersCount = 0L;

        // Xử lý danh sách đơn hàng
        List<OrderResponse> recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(order -> OrderResponse.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .totalAmount(order.getTotalAmount())
                        .employeeName(order.getEmployee()!= null? order.getEmployee().getFullName() : "Khách vãng lai")
                        .storeName(order.getStore()!= null? order.getStore().getName() : "N/A")
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Lấy Top nhân viên


        // Tính toán tăng trưởng
        BigDecimal lastMonthRevenue = orderRepository.getTotalRevenue(lastMonthStart, lastMonthEnd, storeId);
        double revGrowth = 0;
        if (lastMonthRevenue!= null && lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revGrowth = currentRevenue.subtract(lastMonthRevenue)
                    .multiply(new BigDecimal("100"))
                    .divide(lastMonthRevenue, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return DashboardSummaryResponse.builder()
                .totalRevenue(currentRevenue)
                .totalOrders(currentOrdersCount.intValue())
                .totalProducts(productRepository.countTotalProducts().intValue())
                .lowStockProducts(productRepository.countLowStock(10).intValue())
                .recentOrders(recentOrders)
                .revenueGrowth(revGrowth)
                .orderGrowth(5.0)
                .pendingRecommendations(3)
                .build();
    }

    // =========================================================================
    // 2. HÀM DÀNH CHO BÁO CÁO HIỆU SUẤT CỬA HÀNG (ĐÃ KHÔI PHỤC)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<StoreRevenueDTO> getStorePerformanceReport(DashboardFilterRequest request) {
        log.info("Bắt đầu truy xuất báo cáo doanh thu cửa hàng từ {} đến {}",
                request.getStartDate(), request.getEndDate());

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        return orderAnalyticsRepository.getRevenueGroupedByStore(
                OrderStatus.COMPLETED,
                startDateTime,
                endDateTime,
                request.getAreaId()
        );
    }

    // =========================================================================
    // 3. HÀM DÀNH CHO BIỂU ĐỒ XU HƯỚNG DOANH THU (ĐÃ KHÔI PHỤC VÀ FIX LỖI ÉP KIỂU)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<RevenueTrendDTO> getStoreRevenueTrend(Integer storeId, DashboardFilterRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);

        // Đổi thành List<Object[]> để hứng dữ liệu đa cột từ Native Query
        List<Object[]> rawResults = orderAnalyticsRepository.getMonthlyRevenueTrendNative(
                storeId, startDateTime, endDateTime
        );

        // Mapping dữ liệu an toàn
        return rawResults.stream().map(row -> {
            // Đã fix: Lấy từng phần tử trực tiếp từ mảng Object[] row
            String period = (String) row[0];
            Long orderCount = ((Number) row[1]).longValue();
            BigDecimal revenue = (BigDecimal) row[2];
            return new RevenueTrendDTO(period, orderCount, revenue);
        }).collect(Collectors.toList());
    }
}