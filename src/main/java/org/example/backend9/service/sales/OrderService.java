package org.example.backend9.service.sales;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.dto.request.finance.CashbookTransactionRequest;
import org.example.backend9.enums.TransactionType;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.entity.sales.*;
import org.example.backend9.entity.core.*;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.repository.sales.*;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.service.finance.CashbookTransactionService;
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
    private final StoreRepository storeRepository;
    private final CashbookTransactionService cashbookService;
    private final LoyaltyService loyaltyService;

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

        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId()).ifPresent(order::setCustomer);
        }

        Store finalStore = store;
        if (finalStore == null) {
            if (request.getStoreId() != null) {
                finalStore = storeRepository.findById(request.getStoreId())
                        .orElseThrow(() -> new RuntimeException("Cửa hàng không hợp lệ"));
            } else if (employee != null && employee.getStore() != null) {
                finalStore = employee.getStore();
            }
        }
        order.setStore(finalStore);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderDetails(new ArrayList<>());

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (OrderRequest.ItemRequest itemReq : request.getItems()) {
                ProductVariant variant = variantRepository.findById(itemReq.getProductVariantId().longValue())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                variant.setQuantity(variant.getQuantity() - itemReq.getQuantity());
                variantRepository.save(variant);

                ProductPricing pricing = pricingRepository.findByVariantId(variant.getId().longValue()).stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Chưa có giá bán!"));
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
        }

        order.setSubtotal(subtotal);

        BigDecimal manualDiscount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal pointDiscount = BigDecimal.ZERO;

        if (request.getUsedPoints() != null && request.getUsedPoints() > 0 && order.getCustomer() != null) {
            pointDiscount = new BigDecimal(request.getUsedPoints()).multiply(new BigDecimal("100"));
        }

        order.setDiscountAmount(manualDiscount.add(pointDiscount));
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));

        BigDecimal received = BigDecimal.ZERO;
        if (request.getReceivedAmount() != null) {
            received = request.getReceivedAmount();
        } else if (request.getAmountPaid() != null) {
            received = request.getAmountPaid();
        } else {
            received = order.getTotalAmount();
        }

        order.setReceivedAmount(received);

        BigDecimal change = received.subtract(order.getTotalAmount());
        order.setChangeAmount(change.compareTo(BigDecimal.ZERO) > 0 ? change : BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getStatus() == OrderStatus.COMPLETED) {
            handleFinancialAndLoyalty(savedOrder, request);
        }

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateStatus(Integer id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng đã hoàn tất, không thể sửa.");
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) order.setCancelledAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        if (newStatus == OrderStatus.COMPLETED) {
            handleFinancialAndLoyalty(savedOrder, null);
        }

        return mapToResponse(savedOrder);
    }

    private void handleFinancialAndLoyalty(Order order, OrderRequest request) {
        // 1. Khấu trừ điểm đã tiêu
        if (request != null && request.getUsedPoints() != null && request.getUsedPoints() > 0) {
            Customer customer = order.getCustomer();
            if (customer != null) {
                int currentPoints = (customer.getCurrentPoints() == null) ? 0 : customer.getCurrentPoints();
                customer.setCurrentPoints(currentPoints - request.getUsedPoints());
                customerRepository.save(customer);
            }
        }

        // 2. Sinh Phiếu Thu
        CashbookTransactionRequest receiptReq = new CashbookTransactionRequest();
        receiptReq.setType(TransactionType.INCOME);
        receiptReq.setCategory("101 Thu tiền bán hàng");
        receiptReq.setMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : PaymentMethod.CASH);
        receiptReq.setReferenceName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách lẻ");

        BigDecimal amountToCollect = order.getReceivedAmount() != null ? order.getReceivedAmount() : order.getTotalAmount();
        receiptReq.setAmount(amountToCollect);

        receiptReq.setDescription("Thu tiền đơn hàng " + order.getOrderNumber());
        if (order.getStore() != null) receiptReq.setStoreId(order.getStore().getId());
        if (order.getEmployee() != null) receiptReq.setCreatorId(order.getEmployee().getId());
        cashbookService.createTransaction(receiptReq);

        // 3. Sinh Phiếu Chi tiền thừa
        if (order.getPaymentMethod() == PaymentMethod.CASH && order.getChangeAmount() != null && order.getChangeAmount().compareTo(BigDecimal.ZERO) > 0) {
            CashbookTransactionRequest expenseReq = new CashbookTransactionRequest();
            expenseReq.setType(TransactionType.EXPENSE);
            expenseReq.setCategory("700 Trả tiền thừa cho khách");
            expenseReq.setMethod(PaymentMethod.CASH);
            expenseReq.setReferenceName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách lẻ");
            expenseReq.setAmount(order.getChangeAmount());

            expenseReq.setDescription("Trả tiền thừa đơn " + order.getOrderNumber());
            if (order.getStore() != null) expenseReq.setStoreId(order.getStore().getId());
            if (order.getEmployee() != null) expenseReq.setCreatorId(order.getEmployee().getId());
            cashbookService.createTransaction(expenseReq);
        }

        // 4. Tích điểm mới và cập nhật chi tiêu thực tế thông qua LoyaltyService
        if (order.getCustomer() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            try {
                // LoyaltyService sẽ xử lý cả cộng điểm và cộng dồn vào trường totalSpent chuẩn
                loyaltyService.processPointsForOrder(order.getCustomer(), order.getTotalAmount());
            } catch (Exception e) {
                System.err.println("Lỗi tích điểm/chi tiêu: " + e.getMessage());
            }
        }
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
                .receivedAmount(order.getReceivedAmount() != null ? order.getReceivedAmount() : order.getTotalAmount())
                .changeAmount(order.getChangeAmount() != null ? order.getChangeAmount() : BigDecimal.ZERO)
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