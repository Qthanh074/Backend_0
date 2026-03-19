package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.InventoryCheckRequest;
import org.example.backend9.dto.response.inventory.InventoryCheckResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.inventory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryCheckServiceTest {

    @Mock private InventoryCheckRepository checkRepository;
    @Mock private InventoryCheckDetailRepository detailRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductPricingRepository pricingRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private InventoryCheckService inventoryCheckService;

    private Store mockStore;
    private ProductVariant mockVariant;
    private InventoryCheck mockCheck;

    @BeforeEach
    void setUp() {
        mockStore = new Store();
        mockStore.setId(1); // Nếu ID Store là Integer
        mockStore.setName("Cửa hàng HN");

        mockVariant = new ProductVariant();
        mockVariant.setId(100); // ProductVariant thường dùng Long
        mockVariant.setQuantity(50);
        mockVariant.setVariantName("Laptop Dell");
        mockVariant.setSku("DELL-001");

        mockCheck = new InventoryCheck();
        mockCheck.setId(1); // Nếu ID phiếu kiểm là Integer
        mockCheck.setStatus("Đang kiểm");
        mockCheck.setStore(mockStore);
    }

    @Test
    @DisplayName("1. Create: Tạo phiếu kiểm kho mới thành công")
    void create_Success() {
        InventoryCheckRequest req = new InventoryCheckRequest();
        req.setStoreId(1); // Sửa cho khớp kiểu dữ liệu trong DTO

        InventoryCheckRequest.InventoryCheckDetailRequest dReq = new InventoryCheckRequest.InventoryCheckDetailRequest();
        dReq.setProductVariantId(100L); // Dùng Long literal
        dReq.setActualQuantity(45);
        req.setDetails(List.of(dReq));

        // Ép kiểu tường minh cho các mock
        when(storeRepository.findById(anyInt())).thenReturn(Optional.of(mockStore));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(new ProductPricing()));
        when(checkRepository.save(any(InventoryCheck.class))).thenReturn(mockCheck);

        InventoryCheckResponse res = inventoryCheckService.create(req);

        assertNotNull(res);
        verify(detailRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("2. Update: Lỗi khi cố sửa phiếu đã cân bằng")
    void update_Fail_AlreadyBalanced() {
        mockCheck.setStatus("Đã cân bằng");
        // Đảm bảo tham số truyền vào findById khớp kiểu Integer
        when(checkRepository.findById(1)).thenReturn(Optional.of(mockCheck));

        assertThrows(RuntimeException.class, () -> inventoryCheckService.update(1, new InventoryCheckRequest()));
    }

    @Test
    @DisplayName("3. BalanceInventory: Cân bằng kho - Tồn kho hệ thống phải đổi thành tồn thực tế")
    void balanceInventory_Success() {
        // 1. Giả lập Store & InventoryCheck
        InventoryCheck check = new InventoryCheck();
        check.setId(1);
        check.setStatus("Đang kiểm");
        check.setStore(mockStore); // Phải có store để tránh lỗi Null ở mapToResponse

        // 2. Giả lập Detail với đầy đủ các trường (Tránh NullPointerException)
        InventoryCheckDetail detail = new InventoryCheckDetail();
        detail.setProductVariant(mockVariant);
        detail.setSystemQuantity(20);
        detail.setActualQuantity(15);
        detail.setDiscrepancy(-5); // FIX: Set giá trị để không bị Null
        detail.setUnitCost(BigDecimal.valueOf(1000)); // FIX: Set giá trị để tính discrepancyValue

        when(checkRepository.findById(1)).thenReturn(Optional.of(check));
        when(detailRepository.findByInventoryCheckId(1)).thenReturn(List.of(detail));
        when(checkRepository.save(any())).thenReturn(check);

        // Chạy hàm thực tế
        InventoryCheckResponse res = inventoryCheckService.balanceInventory(1);

        // Kiểm tra kết quả
        assertEquals(15, mockVariant.getQuantity()); // Hệ thống cập nhật về tồn thực tế
        assertEquals("Đã cân bằng", res.getStatus());
        verify(variantRepository).save(mockVariant);
    }
}