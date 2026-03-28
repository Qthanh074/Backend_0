package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.request.inventory.VariantRequest;
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.dto.response.inventory.VariantDetailResponse;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.Category;
import org.example.backend9.entity.inventory.Product;
import org.example.backend9.entity.inventory.ProductPricing;
import org.example.backend9.entity.inventory.ProductVariant;
import org.example.backend9.entity.inventory.Unit;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.SupplierRepository;
import org.example.backend9.repository.inventory.CategoryRepository;
import org.example.backend9.repository.inventory.ProductPricingRepository;
import org.example.backend9.repository.inventory.ProductRepository;
import org.example.backend9.repository.inventory.ProductVariantRepository;
import org.example.backend9.repository.inventory.UnitRepository;
import org.example.backend9.service.GoogleSheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private GoogleSheetService googleSheetService;
    @Mock private ProductVariantService variantService;

    // 2 Mock cho tính năng Delete
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductPricingRepository pricingRepository;

    private ProductService productService;

    private Product mockProduct;
    private ProductRequest mockRequest;
    private Category mockCat;
    private Supplier mockSup;
    private Unit mockUnit;

    @BeforeEach
    void setUp() {
        // TỰ KHỞI TẠO BẰNG TAY ĐỂ TRÁNH LỖI LOMBOK KHÔNG INJECT ĐƯỢC 2 REPO MỚI
        productService = new ProductService(
                productRepository, categoryRepository, supplierRepository, unitRepository,
                googleSheetService, variantService, variantRepository, pricingRepository
        );

        // Giả lập Entity Product
        mockProduct = new Product();
        mockProduct.setId(1); // Cực kỳ quan trọng để hàm .longValue() không bị lỗi
        mockProduct.setName("Áo thun Polo");
        mockProduct.setCode("SP001");
        mockProduct.setStatus(EntityStatus.ACTIVE);

        // Khởi tạo sẵn các khóa ngoại
        mockCat = new Category(); mockCat.setId(10); mockCat.setName("Thời trang");
        mockSup = new Supplier(); mockSup.setId(20); mockSup.setName("NCC Tổng");
        mockUnit = new Unit(); mockUnit.setId(30); mockUnit.setName("Cái");

        mockProduct.setCategory(mockCat);
        mockProduct.setSupplier(mockSup);
        mockProduct.setUnit(mockUnit);

        // Giả lập Request
        mockRequest = new ProductRequest();
        mockRequest.setName("Áo thun Polo");
        mockRequest.setCategoryId(10L);
        mockRequest.setSupplierId(20L);
        mockRequest.setUnitId(30L);
        mockRequest.setStatus(EntityStatus.ACTIVE);

        // Giả lập có 1 Variant gửi kèm
        ProductRequest.VariantRequest varReq = new ProductRequest.VariantRequest();
        varReq.setSku("SKU-123");
        varReq.setQuantity(100);
        mockRequest.setVariants(List.of(varReq));
    }

    @Test
    @DisplayName("1. GetAll: Trả về danh sách sản phẩm")
    void getAll_Success() {
        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        List<ProductResponse> result = productService.getAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Áo thun Polo", result.get(0).getName());
    }

    @Test
    @DisplayName("2. Create: Thành công khi đầy đủ thông tin")
    void create_Success() {
        // Dùng anyLong() và anyInt() để không bị miss Matcher do convert kiểu dữ liệu
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mockCat));
        when(supplierRepository.findById(anyInt())).thenReturn(Optional.of(mockSup));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(mockUnit));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        when(variantService.createVariant(any(VariantRequest.class))).thenReturn(null);

        ProductResponse res = productService.create(mockRequest);

        assertNotNull(res);
        assertEquals(10L, res.getCategoryId());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(variantService, times(1)).createVariant(any(VariantRequest.class));
    }

    @Test
    @DisplayName("3. Update: Cập nhật thành công")
    void update_Success() {
        Long productId = 1L;

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mockCat));
        when(supplierRepository.findById(anyInt())).thenReturn(Optional.of(mockSup));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(mockUnit));
        OngoingStubbing<Product> productOngoingStubbing = when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        VariantDetailResponse oldVariant = mock(VariantDetailResponse.class);
        when(oldVariant.getId()).thenReturn(99L);
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(List.of(oldVariant));

        ProductResponse res = productService.update(productId, mockRequest);

        assertNotNull(res);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(variantService, times(1)).deleteVariant(99L);
        verify(variantService, times(1)).createVariant(any(VariantRequest.class));
    }

    @Test
    @DisplayName("4. Update: Báo lỗi khi không tìm thấy Sản phẩm")
    void update_Fail_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.update(99L, mockRequest));
        assertTrue(exception.getMessage().contains("Không thấy SP"));
    }

    @Test
    @DisplayName("5. Delete: Xóa tận gốc Sản phẩm -> Biến thể -> Bảng giá")
    void delete_Success() {
        // CHUẨN BỊ
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Giả lập Variant có ID = 100 để test chạy được qua variant.getId().longValue()
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setId(100);
        when(variantRepository.findByProductId(1L)).thenReturn(List.of(mockVariant));

        // Giả lập Pricing liên kết với Variant (VariantId = 100L)
        ProductPricing mockPricing = new ProductPricing();
        mockPricing.setId(500);
        when(pricingRepository.findByVariantId(100L)).thenReturn(List.of(mockPricing));

        // ACT
        String result = productService.delete(productId);

        // ASSERT
        assertEquals("Đã xóa thành công sản phẩm: Áo thun Polo", result);

        // VERIFY: Đảm bảo thứ tự xóa đúng để không dính Foreign Key
        verify(pricingRepository, times(1)).deleteAll(anyList());
        verify(variantRepository, times(1)).deleteAll(anyList());
        verify(productRepository, times(1)).delete(mockProduct);
    }

    @Test
    @DisplayName("6. Create: Xử lý an toàn khi mất mạng Google Sheets")
    void create_GoogleSheetSync_ThrowsException() {
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        doThrow(new RuntimeException("Mất mạng")).when(googleSheetService).appendRowToSheet(anyString(), anyList());

        assertDoesNotThrow(() -> productService.create(mockRequest));
    }
}