package org.example.backend9.repository.sales;

import org.example.backend9.dto.response.*;
import org.example.backend9.entity.sales.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 🟢 Dùng cho ReportService - Doanh thu (Đã fix lỗi ONLY_FULL_GROUP_BY bằng Native Query)
    @Query(value = """
            SELECT 
                DATE_FORMAT(o.created_at, '%Y-%m-%d') AS period, 
                SUM(o.total_amount) AS revenue, 
                CAST(SUM(o.total_amount * 0.3) AS DECIMAL(38,2)) AS profit, 
                COUNT(o.id) AS orders 
            FROM orders o 
            WHERE o.created_at BETWEEN :start AND :end 
              AND (:storeId IS NULL OR o.store_id = :storeId) 
            GROUP BY DATE_FORMAT(o.created_at, '%Y-%m-%d')
            ORDER BY period ASC
            """, nativeQuery = true)
    List<Object[]> getRevenueReportNative(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("storeId") Long storeId
    );

    // 🟢 Dùng cho ReportService - Top Sản phẩm (4 Tham số)
    @Query("SELECT new org.example.backend9.dto.response.ProductReportResponse(" +
            "CAST(pv.id AS long), p.name, SUM(od.quantity), " +
            "SUM(od.unitPrice * od.quantity), CAST(pv.quantity AS int)) " +
            "FROM Order o JOIN o.orderDetails od JOIN od.productVariant pv JOIN pv.product p " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND (:storeId IS NULL OR o.store.id = :storeId) " +
            "GROUP BY pv.id, p.name, pv.quantity ORDER BY SUM(od.quantity) DESC")
    List<ProductReportResponse> getTopSellingProducts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable,
            @Param("storeId") Long storeId
    );

    // 🟢 Dùng cho ReportService - So sánh cửa hàng
    @Query("SELECT new org.example.backend9.dto.response.StorePerformanceResponse(" +
            "CAST(s.id AS long), s.name, SUM(o.totalAmount), " +
            "CAST(500000000 AS bigdecimal), COUNT(o), COUNT(DISTINCT o.customer.id)) " +
            "FROM Order o JOIN o.store s WHERE o.createdAt BETWEEN :start AND :end " +
            "GROUP BY s.id, s.name")
    List<StorePerformanceResponse> getStoreComparisonReport(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 🟡 Dùng cho Dashboard
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND (:storeId IS NULL OR o.store.id = :storeId)")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("storeId") Long storeId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND (:storeId IS NULL OR o.store.id = :storeId)")
    Long getTotalOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("storeId") Long storeId);

    // 🟡 Lọc đơn hàng gần đây
    @Query("SELECT o FROM Order o WHERE (:storeId IS NULL OR o.store.id = :storeId) ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("storeId") Long storeId, Pageable pageable);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query(value = """
            SELECT 
                e.code AS employeeCode, 
                e.fullName AS fullName, 
                s.name AS storeName, 
                SUM(o.totalAmount) AS totalRevenue, 
                COUNT(o.id) AS orderCount 
            FROM orders o 
            JOIN employees e ON o.employee_id = e.id 
            JOIN stores s ON o.store_id = s.id 
            WHERE o.createdAt BETWEEN :start AND :end 
              AND (:storeId IS NULL OR o.store_id = :storeId) 
            GROUP BY e.id, e.code, e.fullName, s.name 
            ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<Object[]> getEmployeePerformanceNative(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("storeId") Long storeId
    );
}