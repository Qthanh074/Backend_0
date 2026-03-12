package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.entity.sales.Order;
import org.example.backend9.entity.sales.OrderDetail;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
// IMPORT THÊM 2 ENUM NÀY
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;

import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.sales.CustomerRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final CustomerRepository customerRepository;
    private final GoogleSheetService googleSheetService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Employee employee, Store store) {
        Order order = new Order();
        order.setOrderNumber("HD-" + System.currentTimeMillis());
        order.setOrderType(request.getOrderType());

        // FIX LỖI ENUM: Chuyển String từ Request thành Enum
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        }

        // FIX LỖI ENUM: Gán Enum OrderStatus
        order.setStatus(OrderStatus.COMPLETED);

        order.setEmployee(employee);
        order.setStore(store);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            // FIX LỖI INTEGER -> LONG
            Long variantId = itemReq.getProductVariantId().longValue();

            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể ID: " + variantId));

            if (variant.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getVariantName() + " không đủ tồn kho!");
            }

            variant.setQuantity(variant.getQuantity() - itemReq.getQuantity());
            variantRepository.save(variant);

            // FIX LỖI INTEGER -> LONG
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
        order.setTotalAmount(subtotal.add(shippingFee).subtract(discount));

        int earnedPoints = 0;

        if (request.getCustomerId() != null) {
            Integer customerId = request.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer != null) {
                earnedPoints = order.getTotalAmount().divide(new BigDecimal(100000)).intValue();

                customer.setTotalSpent(customer.getTotalSpent().add(order.getTotalAmount()));

                // FIX LỖI KIỂU DỮ LIỆU ĐIỂM TÍCH LŨY
                int currentPoints = customer.getCurrentPoints() != null ? customer.getCurrentPoints().intValue() : 0;
                customer.setCurrentPoints(currentPoints + earnedPoints);

                order.setCustomer(customer);
            }
        }

        Order savedOrder = orderRepository.save(order);

        try {
            List<Object> rowData = Arrays.asList(
                    savedOrder.getOrderNumber(),
                    savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt().toString() : LocalDateTime.now().toString(),
                    savedOrder.getCustomer() != null ? savedOrder.getCustomer().getFullName() : "Khách lẻ",
                    savedOrder.getTotalAmount().toString(),
                    // FIX LỖI ENUM -> STRING ĐỂ ĐẨY LÊN SHEET
                    savedOrder.getPaymentMethod() != null ? savedOrder.getPaymentMethod().name() : "CASH",
                    savedOrder.getOrderType()
            );
            googleSheetService.appendRowToSheet("Order", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets: " + e.getMessage());
        }

        return OrderResponse.builder()
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalAmount())
                .discountAmount(discount)
                .earnedPoints(earnedPoints)
                // FIX LỖI ENUM -> STRING ĐỂ TRẢ VỀ RESPONSE
                .status(savedOrder.getStatus().name())
                .build();
    }
}