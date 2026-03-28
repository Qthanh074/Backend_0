package org.example.backend9.service.sales;

import org.example.backend9.dto.response.*;
import org.example.backend9.repository.inventory.CategoryRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ReportService reportService;

    private String startDate;
    private String endDate;

    @BeforeEach
    void setUp() {
        startDate = "2024-03-01";
        endDate = "2024-03-31";
    }

    @Test
    @DisplayName("1. Test lấy báo cáo doanh thu thành công")
    void getRevenue_Success() {
        List<Object[]> mockData = new ArrayList<>();
        // [period, revenue, profit, orders]
        mockData.add(new Object[]{"2024-03", new BigDecimal("1000000"), new BigDecimal("200000"), 10L});

        when(orderRepository.getRevenueReportNative(any(), any(), any())).thenReturn(mockData);

        List<RevenueReportResponse> result = reportService.getRevenue(startDate, endDate, "month", 1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000000"), result.get(0).getRevenue());
        assertEquals(10L, result.get(0).getOrders());
    }

    @Test
    @DisplayName("2. Test lấy Top sản phẩm bán chạy")
    void getTopProducts_Success() {
        ProductReportResponse p1 = new ProductReportResponse(1L, "Sản phẩm A", 50L, new BigDecimal("5000000"), 100);
        when(orderRepository.getTopSellingProducts(any(), any(), any(Pageable.class), any()))
                .thenReturn(List.of(p1));

        List<ProductReportResponse> result = reportService.getTopProducts(startDate, endDate, 5, 1L);

        assertNotNull(result);
        assertEquals("Sản phẩm A", result.get(0).getProductName());
    }

    @Test
    @DisplayName("3. Test báo cáo hiệu suất nhân viên")
    void getEmployeePerformance_Success() {
        List<Object[]> mockData = new ArrayList<>();
        // [employeeCode, fullName, storeName, revenue, orderCount]
        mockData.add(new Object[]{"NV001", "Bích Ngọc", "Chi nhánh 1", new BigDecimal("12000000"), 25L});

        when(orderRepository.getEmployeePerformanceNative(any(), any(), any())).thenReturn(mockData);

        List<EmployeePerformanceResponse> result = reportService.getEmployeePerformance(startDate, endDate, 1L);

        assertNotNull(result);
        assertEquals("Bích Ngọc", result.get(0).getFullName());
        assertEquals(120.0, result.get(0).getCompletionRate());
    }

    @Test
    @DisplayName("4. Test so sánh hiệu quả giữa các cửa hàng (DTO MỚI)")
    void getStoreComparison_Success() {
        // CẬP NHẬT THEO DTO MỚI: [storeId, storeName, revenue, target, orders, customers]
        StorePerformanceResponse s1 = new StorePerformanceResponse(
                1L,
                "Chi nhánh Cầu Giấy",
                new BigDecimal("50000000"),
                new BigDecimal("40000000"),
                100L,
                80L
        );

        when(orderRepository.getStoreComparisonReport(any(), any())).thenReturn(List.of(s1));

        List<StorePerformanceResponse> result = reportService.getStoreComparison(startDate, endDate);

        assertNotNull(result);
        assertEquals("Chi nhánh Cầu Giấy", result.get(0).getStoreName());
        assertEquals(new BigDecimal("50000000"), result.get(0).getRevenue());
        assertEquals(80L, result.get(0).getCustomers());
    }
}