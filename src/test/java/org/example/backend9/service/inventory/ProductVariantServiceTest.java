package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.VariantRequest;
import org.example.backend9.dto.response.inventory.VariantDetailResponse;
import org.example.backend9.entity.inventory.*;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.inventory.*;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductVariantServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductPricingRepository pricingRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private GoogleSheetService googleSheetService;

    @InjectMocks
    private ProductVariantService variantService;

    private Product mockProduct;
    private ProductVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("Áo Thun");
        mockProduct.setCode("AT");

        mockVariant = new ProductVariant();
        mockVariant.setId(100);
        mockVariant.setProduct(mockProduct);
        mockVariant.setSku("AT-RED-L");
        mockVariant.setStatus(EntityStatus.ACTIVE);
        mockVariant.setQuantity(10);
    }

    @Test
    @DisplayName("1. Create: Thành công và tự động sinh mã SKU")
    void createVariant_AutoSku_Success() {
        VariantRequest req = new VariantRequest();
        req.setProductId(1L);
        req.setQuantity(5);
        req.setCostPrice(1000.0);
        req.setStatus("ACTIVE");

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(variantRepository.existsBySku(anyString())).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(mockVariant);
        when(pricingRepository.findByVariantId(anyLong())).thenReturn(new ArrayList<>());

        VariantDetailResponse res = variantService.createVariant(req);

        assertNotNull(res);
        verify(variantRepository).save(any(ProductVariant.class));
        verify(pricingRepository).save(any(ProductPricing.class));
        verify(googleSheetService).appendRowToSheet(eq("ProductVariant"), anyList());
    }

    @Test
    @DisplayName("2. Update: Thành công và đổi thông tin giá")
    void updateVariant_Success() {
        VariantRequest req = new VariantRequest();
        req.setQuantity(20);
        req.setCostPrice(2000.0);
        req.setSellPrice(3000.0);

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(new ArrayList<>());
        when(variantRepository.save(any())).thenReturn(mockVariant);

        VariantDetailResponse res = variantService.updateVariant(100L, req);

        assertEquals(20, mockVariant.getQuantity());
        verify(pricingRepository).save(any(ProductPricing.class));
    }

    @Test
    @DisplayName("3. Update: Thất bại khi đổi SKU trùng với biến thể khác")
    void updateVariant_Fail_DuplicateSku() {
        VariantRequest req = new VariantRequest();
        req.setSku("SKU-EXISTED");

        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(variantRepository.existsBySku("SKU-EXISTED")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> variantService.updateVariant(100L, req));
        assertEquals("Mã SKU đã tồn tại: SKU-EXISTED", ex.getMessage());
    }

    @Test
    @DisplayName("4. Delete: Xóa thành công biến thể và bảng giá")
    void deleteVariant_Success() {
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(new ProductPricing()));

        variantService.deleteVariant(100L);

        verify(pricingRepository).deleteAll(anyList());
        verify(variantRepository).delete(mockVariant);
    }

    @Test
    @DisplayName("5. MapToDto: Kiểm tra mapping đầy đủ thông tin")
    void testMapToDto_FullInformation() {
        ProductPricing pricing = new ProductPricing();
        pricing.setBaseCostPrice(5000.0);

        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(pricing));

        // Gọi gián tiếp qua hàm getById
        when(variantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));

        VariantDetailResponse res = variantService.getVariantById(100L);

        assertEquals(5000.0, res.getCostPrice());
        assertEquals("Áo Thun", res.getProductName());
    }
}