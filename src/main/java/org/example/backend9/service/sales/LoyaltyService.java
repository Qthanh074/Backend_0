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
    // private final LoyaltyConfigRepository configRepository; // Nếu bạn có bảng cấu hình hãy tiêm vào đây

    @Transactional
    public void processPointsForOrder(Customer customer, BigDecimal orderTotal) {
        if (customer == null || orderTotal == null) return;

        // 1. Định mức quy đổi: 100,000đ = 1 điểm
        // Mẹo: Nếu bạn đã làm trang "Thiết lập định mức", hãy lấy số từ DB ra ở đây
        BigDecimal exchangeRate = new BigDecimal("100000");

        // 2. Tính số điểm được cộng (Tổng tiền / 100,000)
        // Dùng RoundingMode.FLOOR để 199k vẫn chỉ là 1 điểm cho đúng nghiệp vụ
        int pointsToEarn = orderTotal.divide(exchangeRate, 0, RoundingMode.FLOOR).intValue();

        // 3. Cập nhật số điểm hiện có
        int currentPoints = (customer.getCurrentPoints() == null) ? 0 : customer.getCurrentPoints();
        customer.setCurrentPoints(currentPoints + pointsToEarn);

        // 4. Cập nhật tổng chi tiêu - LƯU Ý: Phải khớp tên field với Customer.java
        // Mình dùng totalSpend (theo lỗi báo ở file OrderService của bạn)
        BigDecimal currentSpend = (customer.getTotalSpend() == null) ? BigDecimal.ZERO : customer.getTotalSpend();
        customer.setTotalSpend(currentSpend.add(orderTotal));

        // 5. Lưu lại
        customerRepository.save(customer);
    }
}