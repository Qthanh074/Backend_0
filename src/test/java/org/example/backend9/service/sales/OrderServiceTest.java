package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.OrderRequest;
import org.example.backend9.dto.response.sales.OrderResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.entity.sales.Loyalty;
import org.example.backend9.entity.sales.Order;
import org.example.backend9.enums.OrderStatus;
import org.example.backend9.enums.PaymentMethod;
import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.sales.CustomerRepository;
import org.example.backend9.repository.sales.LoyaltyRepository;
import org.example.backend9.repository.sales.OrderRepository;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    @Mock private CustomerRepository customerRepository;
    @Mock private LoyaltyRepository loyaltyRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private OrderService orderService;

    private ProductVariant mockVariant;
    private ProductPricing mockPricing;
    private Customer mockCustomer;
    private Employee mockEmployee;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setQuantity(10);
        mockVariant.setVariantName("Sản phẩm Test");

        mockPricing = new ProductPricing();
        mockPricing.setBaseRetailPrice(100000.0); // 100k

        mockCustomer = new Customer();
        mockCustomer.setId(1);
        mockCustomer.setFullName("Ngọc Customer");
        mockCustomer.setTotalSpent(BigDecimal.ZERO);
        mockCustomer.setCurrentPoints(0);

        mockEmployee = new Employee();
        mockStore = new Store();
    }

    @Test
    @DisplayName("1. CreateOrder: Thành công - Trừ kho và tích điểm đúng")
    void createOrder_Success() {
        // Given
        OrderRequest req = new OrderRequest();
        req.setOrderType("RETAIL");
        req.setPaymentMethod("CASH");
        req.setCustomerId(1);
        req.setDiscount(new BigDecimal("10000")); // Giảm 10k

        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(100);
        item.setQuantity(2); // Mua 2 cái = 200k
        req.setItems(List.of(item));

        // Mocking
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(mockPricing));
        when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        // Cấu hình: 10k = 1 điểm
        when(loyaltyRepository.findById(1)).thenReturn(Optional.of(new Loyalty(1, new BigDecimal("10000"), new BigDecimal("100"))));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setOrderNumber("HD-MOCK");
            return o;
        });

        // When: (200k - 10k giảm giá = 190k thanh toán). 190k / 10k rate = 19 điểm.
        OrderResponse res = orderService.createOrder(req, mockEmployee, mockStore);

        // Then
        assertEquals(8, mockVariant.getQuantity()); // 10 - 2 = 8
        assertEquals(new BigDecimal("190000.0"), res.getTotalAmount());
        assertEquals(19, res.getEarnedPoints());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("2. CreateOrder: Thất bại khi kho không đủ hàng")
    void createOrder_Fail_OutOfStock() {
        OrderRequest req = new OrderRequest();
        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(100);
        item.setQuantity(50); // Kho có 10, mua 50
        req.setItems(List.of(item));

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(req, mockEmployee, mockStore));

        assertTrue(ex.getMessage().contains("không đủ tồn kho"));
    }

    @Test
    @DisplayName("3. Logic: Tổng tiền không được âm dù giảm giá nhiều")
    void createOrder_TotalNotNegative() {
        OrderRequest req = new OrderRequest();
        req.setDiscount(new BigDecimal("500000")); // Giảm 500k trong khi đơn hàng có 100k

        OrderRequest.ItemRequest item = new OrderRequest.ItemRequest();
        item.setProductVariantId(100);
        item.setQuantity(1);
        req.setItems(List.of(item));

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(mockPricing));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse res = orderService.createOrder(req, mockEmployee, mockStore);

        assertEquals(BigDecimal.ZERO, res.getTotalAmount());
    }
}