package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ProductRequest;
import org.example.backend9.dto.response.inventory.ProductResponse;
import org.example.backend9.entity.inventory.Category;
import org.example.backend9.entity.inventory.Product;
import org.example.backend9.entity.core.Supplier;
import org.example.backend9.entity.inventory.Unit;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.inventory.CategoryRepository;
import org.example.backend9.repository.inventory.ProductRepository;
import org.example.backend9.repository.inventory.UnitRepository;
import org.example.backend9.repository.core.SupplierRepository;
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
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private GoogleSheetService googleSheetService;
    @Mock private ProductVariantService variantService;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private ProductRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Giả lập Entity Product
        mockProduct = new Product();
        mockProduct.setId(1); // Giả sử ID là Integer trong Entity
        mockProduct.setName("Áo thun Polo");
        mockProduct.setCode("SP001");
        mockProduct.setStatus(EntityStatus.ACTIVE);

        // Giả lập Request
        mockRequest = new ProductRequest();
        mockRequest.setName("Áo thun Polo");
        mockRequest.setCategoryId(10L);
        mockRequest.setSupplierId(20L);
        mockRequest.setUnitId(30L);
        mockRequest.setStatus(EntityStatus.ACTIVE);
        mockRequest.setVariants(new ArrayList<>()); // Để trống để test logic product trước
    }

    @Test
    @DisplayName("1. Create: Thành công khi đầy đủ thông tin")
    void create_Success() {
        // --- FIX: Tạo Category và set ID để tránh NullPointerException ---
        Category mockCat = new Category();
        mockCat.setId(10); // Quan trọng: Phải có ID
        mockCat.setName("Điện tử");

        Supplier mockSup = new Supplier();
        mockSup.setId(20);
        mockSup.setName("NCC Tổng");

        Unit mockUnit = new Unit();
        mockUnit.setId(30);
        mockUnit.setName("Cái");

        // Gán các đối tượng này vào mockProduct để lúc mapping không bị null
        mockProduct.setCategory(mockCat);
        mockProduct.setSupplier(mockSup);
        mockProduct.setUnit(mockUnit);

        // Mock các tìm kiếm
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mockCat));
        when(supplierRepository.findById(anyInt())).thenReturn(Optional.of(mockSup));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(mockUnit));

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        // Thực thi
        ProductResponse res = productService.create(mockRequest);

        // Kiểm tra
        assertNotNull(res);
        assertEquals(10L, res.getCategoryId()); // Kiểm tra xem ID map sang có đúng 10 không
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("2. GetAll: Trả về danh sách sản phẩm")
    void getAll_Success() {
        when(productRepository.findAll()).thenReturn(List.of(mockProduct));
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        List<ProductResponse> result = productService.getAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Áo thun Polo", result.get(0).getName());
    }

    @Test
    @DisplayName("3. Update: Thành công và cập nhật thông tin")
    void update_Success() {
        Long productId = 1L;

        // --- FIX: Tương tự như trên, đảm bảo mockProduct có đầy đủ Category/Supplier/Unit ---
        Category mockCat = new Category(); mockCat.setId(10);
        Supplier mockSup = new Supplier(); mockSup.setId(20);
        Unit mockUnit = new Unit(); mockUnit.setId(30);

        mockProduct.setCategory(mockCat);
        mockProduct.setSupplier(mockSup);
        mockProduct.setUnit(mockUnit);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mockCat));
        when(supplierRepository.findById(anyInt())).thenReturn(Optional.of(mockSup));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(mockUnit));

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(variantService.getVariantsByProductId(anyLong())).thenReturn(new ArrayList<>());

        ProductResponse res = productService.update(productId, mockRequest);

        assertNotNull(res);
        verify(variantService, atLeastOnce()).getVariantsByProductId(productId);
    }

    @Test
    @DisplayName("4. Delete: Thành công")
    void delete_Success() {
        Long productId = 1L;
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));

        String result = productService.delete(productId);

        assertTrue(result.contains("Đã xóa thành công"));
        verify(productRepository).delete(mockProduct);
    }

    @Test
    @DisplayName("5. Error: Ném lỗi khi update sản phẩm không tồn tại")
    void update_Fail_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.update(99L, mockRequest));
    }
}