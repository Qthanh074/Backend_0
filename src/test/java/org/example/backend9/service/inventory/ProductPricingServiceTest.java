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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPricingServiceTest {

    @Mock private ProductPricingRepository pricingRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private ProductPricingService pricingService;

    private Product mockProduct;
    private ProductVariant mockVariant;
    private Store mockStore;
    private ProductPricing mockPricing;
    private ProductPricingRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("Áo Sơ Mi Nam");
        mockProduct.setCode("SM-01");

        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setSku("SKU-SM-01-XL");
        mockVariant.setVariantName("Size XL");
        mockVariant.setProduct(mockProduct);

        mockStore = new Store();
        mockStore.setId(1);
        mockStore.setName("Chi nhánh Cầu Giấy");

        mockPricing = new ProductPricing();
        mockPricing.setId(500);
        mockPricing.setVariant(mockVariant);
        mockPricing.setProduct(mockProduct);
        mockPricing.setStore(mockStore);
        mockPricing.setBaseCostPrice(100000.0);
        mockPricing.setBaseRetailPrice(250000.0);
        mockPricing.setStatus("ACTIVE");

        mockRequest = new ProductPricingRequest();
        mockRequest.setVariantId(100L);
        mockRequest.setStoreId(1);
        mockRequest.setBaseCostPrice(120000.0);
        mockRequest.setBaseRetailPrice(300000.0);
        mockRequest.setWholesalePrice(280000.0);
        mockRequest.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("1. Lấy tất cả bảng giá (getAll) thành công")
    void getAll_Success() {
        when(pricingRepository.findAll()).thenReturn(Collections.singletonList(mockPricing));

        List<ProductPricingResponse> responses = pricingService.getAll();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Áo Sơ Mi Nam - Size XL", responses.get(0).getProductName());
        verify(pricingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("2. SetupPrice: Thêm mới giá khi chưa tồn tại (Kèm Cửa Hàng)")
    void setupPrice_CreateNew_WithStore_Success() {
        // ĐÃ FIX: Chuyển new ArrayList<>() thành Optional.empty()
        when(pricingRepository.findByVariantIdAndStoreId(100L, 1)).thenReturn(Optional.empty());
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        ProductPricingResponse res = pricingService.setupPrice(mockRequest);

        assertNotNull(res);
        assertEquals("Chi nhánh Cầu Giấy", res.getStoreName());
        verify(pricingRepository, times(1)).save(any(ProductPricing.class));
        verify(googleSheetService, times(1)).appendRowToSheet(eq("ProductPricing"), anyList());
    }

    @Test
    @DisplayName("3. SetupPrice: Cập nhật giá cho hệ thống chung (Store = null)")
    void setupPrice_UpdateGlobal_NoStore_Success() {
        mockRequest.setStoreId(null);

        // ĐÃ FIX: Chuyển SingletonList thành Optional.of()
        when(pricingRepository.findByVariantIdAndStoreId(100L, null)).thenReturn(Optional.of(mockPricing));
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        ProductPricingResponse res = pricingService.setupPrice(mockRequest);

        assertNotNull(res);
        verify(storeRepository, never()).findById(any());
        verify(pricingRepository, times(1)).save(any(ProductPricing.class));
    }

    @Test
    @DisplayName("4. SetupPrice: Lỗi do không tìm thấy Variant")
    void setupPrice_VariantNotFound_ThrowsException() {
        // ĐÃ FIX: Optional.empty()
        when(pricingRepository.findByVariantIdAndStoreId(100L, 1)).thenReturn(Optional.empty());
        when(variantRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pricingService.setupPrice(mockRequest));
        assertTrue(exception.getMessage().contains("Không tìm thấy biến thể"));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. SetupPrice: Lỗi do không tìm thấy Store")
    void setupPrice_StoreNotFound_ThrowsException() {
        // ĐÃ FIX: Optional.empty()
        when(pricingRepository.findByVariantIdAndStoreId(100L, 1)).thenReturn(Optional.empty());
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(storeRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pricingService.setupPrice(mockRequest));
        assertTrue(exception.getMessage().contains("Không tìm thấy cửa hàng"));
        verify(pricingRepository, never()).save(any());
    }

    @Test
    @DisplayName("6. Xóa bảng giá (delete) thành công")
    void delete_Success() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));

        String result = pricingService.delete(500);

        assertTrue(result.contains("Đã xóa bảng giá thành công"));
        verify(pricingRepository, times(1)).delete(mockPricing);
    }

    @Test
    @DisplayName("7. Lỗi khi xóa bảng giá không tồn tại")
    void delete_NotFound_ThrowsException() {
        when(pricingRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pricingService.delete(999));
        verify(pricingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("8. Duyệt giá (approvePrice) thành công")
    void approvePrice_Success() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        pricingService.approvePrice(500);

        assertEquals("Đang áp dụng", mockPricing.getStatus());
        verify(pricingRepository, times(1)).save(mockPricing);
        verify(googleSheetService, times(1)).appendRowToSheet(eq("ProductPricing"), anyList());
    }

    @Test
    @DisplayName("9. Tìm kiếm (search) khi chuỗi tìm kiếm rỗng thì trả về GetAll")
    void search_EmptyQuery_ReturnsAll() {
        when(pricingRepository.findAll()).thenReturn(Collections.singletonList(mockPricing));

        List<ProductPricingResponse> responses = pricingService.search("   ");

        assertEquals(1, responses.size());
        verify(pricingRepository, times(1)).findAll();
        verify(pricingRepository, never()).searchPricing(anyString());
    }

    @Test
    @DisplayName("10. Tìm kiếm (search) có chuỗi hợp lệ")
    void search_ValidQuery_ReturnsFiltered() {
        when(pricingRepository.searchPricing("SM-01")).thenReturn(Collections.singletonList(mockPricing));

        List<ProductPricingResponse> responses = pricingService.search("SM-01");

        assertEquals(1, responses.size());
        verify(pricingRepository, times(1)).searchPricing("SM-01");
    }

    @Test
    @DisplayName("11. Lỗi Google Sheets không được làm gián đoạn logic lưu data")
    void syncGoogleSheets_Exception_HandledGracefully() {
        when(pricingRepository.findById(500)).thenReturn(Optional.of(mockPricing));
        when(pricingRepository.save(any(ProductPricing.class))).thenReturn(mockPricing);

        doThrow(new RuntimeException("Mất kết nối API Google")).when(googleSheetService).appendRowToSheet(anyString(), anyList());

        assertDoesNotThrow(() -> pricingService.approvePrice(500));
        assertEquals("Đang áp dụng", mockPricing.getStatus());
    }
}