package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ProductPricingRequest;
import org.example.backend9.dto.response.inventory.ProductPricingResponse;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.inventory.Product;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPricingServiceTest {

    @Mock private ProductPricingRepository pricingRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private ProductPricingService pricingService;

    private ProductVariant mockVariant;
    private Store mockStore;
    private ProductPricing mockPricing;

    @BeforeEach
    void setUp() {
        // Giả lập Product & Variant
        Product mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("Sản phẩm mẫu");
        mockProduct.setCode("SP-MOCK");

        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setSku("SKU-100");
        mockVariant.setProduct(mockProduct);

        mockStore = new Store();
        mockStore.setId(1);
        mockStore.setName("Chi nhánh HN");

        mockPricing = new ProductPricing();
        mockPricing.setId(500);
        mockPricing.setVariant(mockVariant);
        mockPricing.setProduct(mockProduct);
        mockPricing.setStore(mockStore);
        mockPricing.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("1. SetupPrice: Tạo mới giá thành công khi chưa tồn tại")
    void setupPrice_CreateNew_Success() {
        ProductPricingRequest req = new ProductPricingRequest();
        req.setVariantId(100L);
        req.setStoreId(1);
        req.setBaseCostPrice(50000.0);
        req.setBaseRetailPrice(80000.0);

        // Giả lập chưa có bản ghi nào (orElse trả về new ProductPricing)
        when(pricingRepository.findByVariantIdAndStoreId(100L, 1)).thenReturn(new ArrayList<>());
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        ProductPricingResponse res = pricingService.setupPrice(req);

        assertNotNull(res);
        verify(pricingRepository).save(any(ProductPricing.class));
        verify(googleSheetService).appendRowToSheet(eq("ProductPricing"), anyList());
    }

    @Test
    @DisplayName("2. ApprovePrice: Duyệt giá thành công")
    void approvePrice_Success() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        pricingService.approvePrice(500);

        assertEquals("Đang áp dụng", mockPricing.getStatus());
        verify(pricingRepository).save(mockPricing);
    }

    @Test
    @DisplayName("3. Delete: Xóa bảng giá thành công")
    void delete_Success() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));

        String result = pricingService.delete(500);

        assertTrue(result.contains("Đã xóa bảng giá thành công"));
        verify(pricingRepository).delete(mockPricing);
    }

    @Test
    @DisplayName("4. GetById: Lỗi khi không tìm thấy bảng giá")
    void delete_Fail_NotFound() {
        when(pricingRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pricingService.delete(999));
    }

    @Test
    @DisplayName("5. SyncError: Lỗi Google Sheets không làm chết ứng dụng")
    void syncError_ShouldNotBreakFlow() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));
        when(pricingRepository.save(any())).thenReturn(mockPricing);

        // Giả lập Google Sheet ném lỗi
        doThrow(new RuntimeException("Google API Error")).when(googleSheetService).appendRowToSheet(anyString(), anyList());

        assertDoesNotThrow(() -> pricingService.approvePrice(500));
    }
}