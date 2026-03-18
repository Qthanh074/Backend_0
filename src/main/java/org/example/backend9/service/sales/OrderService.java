package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.entity.sales.Order;
import org.example.backend9.entity.sales.OrderDetail;
import org.example.backend9.entity.sales.Loyalty;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;

import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.sales.CustomerRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.repository.sales.LoyaltyRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final CustomerRepository customerRepository;
    private final LoyaltyRepository loyaltyRepository; // ✅ Thêm Repo cấu hình tích điểm
    private final GoogleSheetService googleSheetService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Employee employee, Store store) {
        Order order = new Order();
        order.setOrderNumber("HD-" + System.currentTimeMillis());
        order.setOrderType(request.getOrderType());

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setEmployee(employee);
        order.setStore(store);

        // Đảm bảo list không bị NullPointerException
        if (order.getOrderDetails() == null) {
            order.setOrderDetails(new ArrayList<>());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        // ✅ SỬA LỖI 1: Lấy chiết khấu từ Request (nếu có)
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;

        // Xử lý từng sản phẩm trong giỏ
        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            Long variantId = itemReq.getProductVariantId().longValue();

            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + variantId));

            if (variant.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getVariantName() + " không đủ tồn kho!");
            }

            // Trừ tồn kho
            variant.setQuantity(variant.getQuantity() - itemReq.getQuantity());
            variantRepository.save(variant);

            ProductPricing pricing = pricingRepository.findByVariantId(variantId).stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Sản phẩm chưa được thiết lập giá bán!"));

            BigDecimal unitPrice = BigDecimal.valueOf(pricing.getBaseRetailPrice());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductVariant(variant);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(unitPrice);
            detail.setTotal(unitPrice.multiply(new BigDecimal(itemReq.getQuantity())));

            subtotal = subtotal.add(detail.getTotal());
            order.getOrderDetails().add(detail);
        }

        order.setSubtotal(subtotal);
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;

        // Tính tổng tiền thanh toán: (Tạm tính + Phí Ship) - Khuyến mãi
        order.setTotalAmount(subtotal.add(shippingFee).subtract(discount));

        // Tránh tình trạng âm tiền
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            order.setTotalAmount(BigDecimal.ZERO);
        }

        int earnedPoints = 0;

        // ✅ SỬA LỖI 2: Tích điểm dựa trên cấu hình lấy từ Database
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            if (customer != null) {
                // Lấy cấu hình (Nếu không có thì mặc định 100k = 1đ)
                Loyalty config = loyaltyRepository.findById(1)
                        .orElse(new Loyalty(1, new BigDecimal("100000"), new BigDecimal("100")));

                // Tính điểm bằng: Tổng tiền / Mức quy đổi (Làm tròn xuống)
                if (config.getExchangeRateEarn() != null && config.getExchangeRateEarn().compareTo(BigDecimal.ZERO) > 0) {
                    earnedPoints = order.getTotalAmount().divide(config.getExchangeRateEarn(), 0, RoundingMode.DOWN).intValue();
                }

                customer.setTotalSpent(customer.getTotalSpent().add(order.getTotalAmount()));
                int currentPoints = customer.getCurrentPoints() != null ? customer.getCurrentPoints() : 0;
                customer.setCurrentPoints(currentPoints + earnedPoints);

                order.setCustomer(customer);
            }
        }

        Order savedOrder = orderRepository.save(order);

        // ✅ SỬA LỖI 3: Đẩy việc gọi Google Sheets sang một luồng (thread) chạy ngầm
        CompletableFuture.runAsync(() -> {
            try {
                List<Object> rowData = Arrays.asList(
                        savedOrder.getOrderNumber(),
                        savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt().toString() : LocalDateTime.now().toString(),
                        savedOrder.getCustomer() != null ? savedOrder.getCustomer().getFullName() : "Khách lẻ",
                        savedOrder.getTotalAmount().toString(),
                        savedOrder.getPaymentMethod() != null ? savedOrder.getPaymentMethod().name() : "CASH",
                        savedOrder.getOrderType()
                );
                googleSheetService.appendRowToSheet("Order", rowData);
            } catch (Exception e) {
                System.err.println("Lỗi đồng bộ Google Sheets: " + e.getMessage());
            }
        });

        return OrderResponse.builder()
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalAmount())
                .discountAmount(discount)
                .earnedPoints(earnedPoints)
                .status(savedOrder.getStatus().name())
                .build();
    }
}