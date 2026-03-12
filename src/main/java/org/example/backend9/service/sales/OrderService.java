package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.sales.*;
import org.example.backend9.entity.core.*;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.repository.sales.CustomerRepository;
import org.example.backend9.service.GoogleSheetService; // Import service của bạn
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

//    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final CustomerRepository customerRepository;
    private final GoogleSheetService googleSheetService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Employee employee, Store store) {
        // 1. Khởi tạo Order Entity
        Order order = new Order();
        order.setOrderNumber("HD-" + System.currentTimeMillis());
        order.setOrderType(request.getOrderType());
        order.setEmployee(employee);
        order.setStore(store);

        BigDecimal subtotal = BigDecimal.ZERO;

        // 2. Xử lý từng sản phẩm & Trừ kho
        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + itemReq.getProductVariantId()));

            if (variant.getInventory() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getName() + " không đủ tồn kho!");
            }

            // Trừ tồn kho
            variant.setInventory(variant.getInventory() - itemReq.getQuantity());
            variantRepository.save(variant);

            // Tạo chi tiết đơn hàng
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductVariant(variant);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(variant.getPrice());
            detail.setTotal(variant.getPrice().multiply(new BigDecimal(itemReq.getQuantity())));

            subtotal = subtotal.add(detail.getTotal());
            order.getOrderDetails().add(detail);
        }

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO));

        // 3. Tích điểm nếu có khách hàng
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            if (customer != null) {
                customer.setTotalSpent(customer.getTotalSpent().add(order.getTotalAmount()));
                customer.setCurrentPoints(customer.getCurrentPoints() + order.getTotalAmount().divide(new BigDecimal(100000)).intValue());
                order.setCustomer(customer);
            }
        }

        // 4. Lưu vào Database
        Order savedOrder = orderRepository.save(order);

        // 5. Đẩy dữ liệu lên Google Sheet (Tên sheet là "Order")
        List<Object> rowData = Arrays.asList(
                savedOrder.getOrderNumber(),
                savedOrder.getCreatedAt().toString(),
                savedOrder.getCustomer() != null ? savedOrder.getCustomer().getFullName() : "Khách lẻ",
                savedOrder.getTotalAmount().toString(),
                savedOrder.getPaymentMethod() != null ? savedOrder.getPaymentMethod().toString() : "CASH",
                savedOrder.getOrderType()
        );

        try {
            googleSheetService.appendRowToSheet("Order", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets: " + e.getMessage());
        }

        return OrderResponse.fromEntity(savedOrder);
    }
}