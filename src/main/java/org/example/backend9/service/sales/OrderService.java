package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.entity.sales.*;
import org.example.backend9.entity.core.*;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.sales.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPricingRepository pricingRepository;
    private final CustomerRepository customerRepository;
    private final org.example.backend9.repository.core.EmployeeRepository employeeRepository;

    public List<OrderResponse> getOrdersByFilter(String channel, String status, String type) {
        return orderRepository.findAll().stream()
                .filter(order -> {
                    if ("HISTORY".equalsIgnoreCase(type)) return true;
                    boolean match = true;
                    if (channel != null && !channel.isEmpty()) {
                        match = match && channel.equalsIgnoreCase(order.getOrderType());
                    }
                    if (status != null && !status.isEmpty()) {
                        match = match && order.getStatus() != null &&
                                order.getStatus().name().equalsIgnoreCase(status);
                    }
                    return match;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Employee employee, Store store) {
        Order order = new Order();
        order.setOrderNumber("HD-" + System.currentTimeMillis());
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : "RETAIL");

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        }

        order.setStatus("ONLINE".equals(order.getOrderType()) ? OrderStatus.PENDING : OrderStatus.COMPLETED);
        order.setEmployee(employee);
        order.setStore(store);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderDetails(new ArrayList<>());

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (OrderRequest.ItemRequest itemReq : request.getItems()) {
                ProductVariant variant = variantRepository.findById(itemReq.getProductVariantId().longValue())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                variant.setQuantity(variant.getQuantity() - itemReq.getQuantity());
                variantRepository.save(variant);

                // FIX LỖI 1: Tự động lấy giá bán từ Database thay vì từ itemReq (Bảo mật hơn)
                ProductPricing pricing = pricingRepository.findByVariantId(variant.getId().longValue()).stream().findFirst()                        .orElseThrow(() -> new RuntimeException("Chưa có giá bán cho sản phẩm này!"));
                BigDecimal unitPrice = BigDecimal.valueOf(pricing.getBaseRetailPrice());

                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProductVariant(variant);
                detail.setQuantity(itemReq.getQuantity());

                // Gán giá vừa lấy từ DB vào đây
                detail.setUnitPrice(unitPrice);
                detail.setTotal(unitPrice.multiply(new BigDecimal(itemReq.getQuantity())));

                subtotal = subtotal.add(detail.getTotal());
                order.getOrderDetails().add(detail);
            }
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Integer id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id));
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
        }
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "CASH")
                .status(order.getStatus() != null ? order.getStatus().name() : "PENDING")
                .subTotal(order.getSubtotal())
                .discount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách lẻ")
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : "-")
                .employeeName(order.getEmployee() != null ? order.getEmployee().getFullName() : "-")
                .storeName(order.getStore() != null ? order.getStore().getName() : "-")
                .createdAt(order.getCreatedAt())
                .items(order.getOrderDetails() != null ? order.getOrderDetails().stream().map(d -> {
                    String pName = "Sản phẩm";
                    String vName = "";
                    if (d.getProductVariant() != null) {
                        vName = d.getProductVariant().getVariantName();
                        if (d.getProductVariant().getProduct() != null) {
                            pName = d.getProductVariant().getProduct().getName();
                        }
                    }
                    return OrderResponse.OrderItemResponse.builder()
                            .productName(pName)
                            .variantName(vName)
                            .quantity(d.getQuantity())
                            .unitPrice(d.getUnitPrice())
                            .totalPrice(d.getTotal())
                            .build();
                }).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}