package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.entity.sales.Loyalty;
import org.example.backend9.repository.sales.CustomerRepository;
import org.example.backend9.repository.sales.LoyaltyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final CustomerRepository customerRepository;
    private final LoyaltyRepository loyaltyRepository; // 🟢 Tiêm Repository để lấy cấu hình từ DB

    @Transactional
    public void processPointsForOrder(Customer customer, BigDecimal orderTotal) {
        if (customer == null || orderTotal == null) return;

        // 1. Lấy định mức (VD: 100,000đ = 1 điểm)
        BigDecimal exchangeRate = loyaltyRepository.findById(1)
                .map(Loyalty::getExchangeRateEarn)
                .orElse(new BigDecimal("100000"));

        // 2. Cộng điểm
        int pointsToEarn = orderTotal.divide(exchangeRate, 0, RoundingMode.FLOOR).intValue();
        customer.setCurrentPoints((customer.getCurrentPoints() == null ? 0 : customer.getCurrentPoints()) + pointsToEarn);

        // 3. CỘNG DỒN CHI TIÊU (Đây là chỗ làm tăng số tiền tiêu dùng)
        BigDecimal currentSpent = (customer.getTotalSpent() == null) ? BigDecimal.ZERO : customer.getTotalSpent();
        customer.setTotalSpent(currentSpent.add(orderTotal)); // Cập nhật vào totalSpent

        customerRepository.save(customer);
    }
}