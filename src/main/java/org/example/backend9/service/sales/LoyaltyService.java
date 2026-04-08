package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final CustomerRepository customerRepository;

    @Transactional
    public void processPointsForOrder(Customer customer, BigDecimal orderTotal) {
        if (customer == null || orderTotal == null) return;

        // 1. Định mức quy đổi: 100,000đ = 1 điểm
        // (Sau này bạn có thể lấy số này từ database nếu có bảng cấu hình)
        BigDecimal exchangeRate = new BigDecimal("100000");

        // 2. Tính số điểm được cộng (Tổng tiền / 100,000)
        // Ví dụ: Đơn 250k sẽ được 2 điểm
        int pointsToEarn = orderTotal.divide(exchangeRate, 0, RoundingMode.DOWN).intValue();

        // 3. Cập nhật dữ liệu khách hàng
        if (pointsToEarn > 0) {
            int currentPoints = (customer.getCurrentPoints() == null) ? 0 : customer.getCurrentPoints();
            customer.setCurrentPoints(currentPoints + pointsToEarn);
        }

        // 4. Cộng dồn vào tổng chi tiêu (Để hệ thống tự nhảy Rank Bạc/Vàng ở trang danh sách)
        BigDecimal currentSpending = (customer.getTotalSpending() == null) ? BigDecimal.ZERO : customer.getTotalSpending();
        customer.setTotalSpending(currentSpending.add(orderTotal));

        // 5. Lưu lại vào Database
        customerRepository.save(customer);
    }
}