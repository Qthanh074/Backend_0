package org.example.backend9.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.sales.Order;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalProducts;
    private Integer lowStockProducts;
    private Double revenueGrowth;
    private Double orderGrowth;
    private List<OrderResponse> recentOrders;
    private List<?> topEmployees;

    // 🟢 THÊM DÒNG NÀY VÀO ĐỂ HẾT LỖI Ở DASHBOARD SERVICE
    private Integer pendingRecommendations;
}