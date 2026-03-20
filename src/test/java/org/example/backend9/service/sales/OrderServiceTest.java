package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.sales.Order;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductPricingRepository pricingRepository;
    @Mock private org.example.backend9.repository.core.EmployeeRepository employeeRepository;

    @InjectMocks
    private OrderService orderService;

    private ProductVariant mockVariant;
    private ProductPricing mockPricing;
    private Employee mockEmployee;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        // Giả lập sản phẩm iPhone 15
        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setQuantity(10);
        mockVariant.setVariantName("iPhone 15 Pro Max");

        // Giả lập giá bán 30.000.000đ
        mockPricing = new ProductPricing();
        mockPricing.setBaseRetailPrice(30000000.0);

        mockEmployee = new Employee();
        mockEmployee.setFullName("Nguyễn Thị Bích Ngọc");

        mockStore = new Store();
        mockStore.setName("CMC Store Hà Nội");
    }

    @Test
    @DisplayName("Test 1: Tạo đơn hàng thành công - Tính tiền và trừ kho chuẩn")
    void createOrder_FullSuccess() {
        // 1. Chuẩn bị Request: Mua 2 cái iPhone, giảm giá 1 triệu
        OrderRequest req = new OrderRequest();
        req.setOrderType("RETAIL");
        req.setPaymentMethod("CASH");
        req.setDiscount(new BigDecimal("1000000"));

        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(100);
        item.setQuantity(2);
        req.setItems(List.of(item));

        // 2. Mocking logic
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(mockPricing));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1);
            o.setCreatedAt(LocalDateTime.now());
            return o;
        });

        // 3. Thực thi
        OrderResponse res = orderService.createOrder(req, mockEmployee, mockStore);

        // 4. Kiểm tra (Assertions)
        // Tổng tiền: (30tr * 2) - 1tr = 59tr
        assertTrue(new BigDecimal("59000000").compareTo(res.getTotalAmount()) == 0);
        // Tồn kho: 10 - 2 = 8
        assertEquals(8, mockVariant.getQuantity());
        assertEquals("COMPLETED", res.getStatus());

        verify(variantRepository, times(1)).save(any(ProductVariant.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Test 2: Lỗi khi sản phẩm không tồn tại trong hệ thống")
    void createOrder_ProductNotFound() {
        OrderRequest req = new OrderRequest();
        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(999);
        req.setItems(List.of(item));

        when(variantRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(req, mockEmployee, mockStore));

        assertEquals("Sản phẩm không tồn tại", ex.getMessage());
    }

    @Test
    @DisplayName("Test 3: Lỗi khi sản phẩm chưa được thiết lập giá bán")
    void createOrder_NoPricing() {
        OrderRequest req = new OrderRequest();
        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(100);
        item.setQuantity(1);
        req.setItems(List.of(item));

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(new ArrayList<>()); // Trả về list rỗng

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(req, mockEmployee, mockStore));

        assertEquals("Chưa có giá bán cho sản phẩm này!", ex.getMessage());
    }

    @Test
    @DisplayName("Test 4: Cập nhật trạng thái đơn hàng sang CANCELLED")
    void updateStatus_ToCancelled() {
        Order order = new Order();
        order.setId(1);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse res = orderService.updateStatus(1, OrderStatus.CANCELLED);

        assertEquals("CANCELLED", res.getStatus());
        verify(orderRepository).save(order);
    }
}