package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO đại diện cho một dòng dữ liệu doanh thu của một cửa hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRevenueDTO {
    private Integer storeId;
    private String storeCode;
    private String storeName;
    private String areaName;

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;

    // AOV sẽ được tính toán động (Calculated Property)
    private BigDecimal averageOrderValue;

    // Constructor đặc biệt dùng cho truy vấn JPQL SELECT NEW
    public StoreRevenueDTO(Integer storeId, String storeCode, String storeName, String areaName,
                           Long totalOrders, BigDecimal totalRevenue, BigDecimal totalDiscount) {
        this.storeId = storeId;
        this.storeCode = storeCode;
        this.storeName = storeName;
        this.areaName = areaName;
        this.totalOrders = totalOrders!= null? totalOrders : 0L;
        this.totalRevenue = totalRevenue!= null? totalRevenue : BigDecimal.ZERO;
        this.totalDiscount = totalDiscount!= null? totalDiscount : BigDecimal.ZERO;

        // Tính toán AOV ngay khi khởi tạo để tránh logic phức tạp tại tầng Service
        if (this.totalOrders > 0) {
            this.averageOrderValue = this.totalRevenue.divide(new BigDecimal(this.totalOrders), 2, RoundingMode.HALF_UP);
        } else {
            this.averageOrderValue = BigDecimal.ZERO;
        }
    }
}