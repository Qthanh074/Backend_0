package org.example.backend9.repository;

import org.example.backend9.entity.sales.Order;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.dto.response.StoreRevenueDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderAnalyticsRepository extends JpaRepository<Order, Integer> {

    // =========================================================
    // 1. HÀM TÍNH DOANH THU THEO CỬA HÀNG (Hàm đang bị báo lỗi thiếu)
    // =========================================================
    @Query("""
        SELECT new org.example.backend9.dto.response.StoreRevenueDTO(
            s.id, s.code, s.name, a.name, 
            COUNT(o.id), SUM(o.totalAmount), SUM(o.discountAmount)
        )
        FROM Order o 
        JOIN o.store s
        LEFT JOIN s.area a
        WHERE o.status = :status 
          AND o.createdAt >= :startDate 
          AND o.createdAt <= :endDate
          AND (:areaId IS NULL OR a.id = :areaId)
        GROUP BY s.id, s.code, s.name, a.name
        ORDER BY SUM(o.totalAmount) DESC
    """)
    List<StoreRevenueDTO> getRevenueGroupedByStore(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("areaId") Integer areaId
    );

    @Query(value = """
        SELECT 
            DATE_FORMAT(o.createdAt, '%Y-%m') AS period, 
            COUNT(o.id) AS orderCount, 
            SUM(o.totalAmount) AS revenue 
        FROM orders o 
        WHERE o.store_id = :storeId 
          AND o.createdAt >= :startDate 
          AND o.createdAt <= :endDate 
          AND o.status = 'COMPLETED'
        GROUP BY DATE_FORMAT(o.createdAt, '%Y-%m') 
        ORDER BY period ASC
    """, nativeQuery = true)
    List<Object[]> getMonthlyRevenueTrendNative(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}