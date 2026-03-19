package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.CategoryRequest;
import org.example.backend9.dto.response.inventory.CategoryResponse;
import org.example.backend9.entity.inventory.Category;
import org.example.backend9.repository.inventory.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.example.backend9.enums.EntityStatus;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;
    private CategoryRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setId(1); // Giả sử ID là Integer hoặc Long tùy Entity của bạn
        mockCategory.setName("Điện thoại");

        mockRequest = new CategoryRequest();
        mockRequest.setName("Laptop");
        mockRequest.setDescription("Mô tả Laptop");
        mockRequest.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("1. GetAll: Trả về danh sách danh mục")
    void getAll_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));

        List<CategoryResponse> result = categoryService.getAll();

        assertEquals(1, result.size());
        assertEquals("Điện thoại", result.get(0).getName());
    }

    @Test
    @DisplayName("2. Create: Thành công (không có danh mục cha)")
    void create_Success_NoParent() {
        when(categoryRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse res = categoryService.create(mockRequest);

        assertNotNull(res);
        assertEquals("Laptop", res.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("3. Create: Thất bại do trùng tên")
    void create_Fail_DuplicateName() {
        when(categoryRepository.existsByName("Laptop")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.create(mockRequest));
        assertEquals("Tên danh mục này đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("4. Create: Thành công với danh mục cha")
    void create_Success_WithParent() {
        mockRequest.setParentId(1L);
        when(categoryRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse res = categoryService.create(mockRequest);

        assertEquals("Điện thoại", res.getParentName());
    }

    @Test
    @DisplayName("5. Update: Thành công và đổi tên")
    void update_Success() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse res = categoryService.update(id, mockRequest);

        assertEquals("Laptop", res.getName());
    }

    @Test
    @DisplayName("6. Update: Lỗi khi tự làm cha chính mình")
    void update_Fail_SelfParenting() {
        Long id = 1L;
        mockRequest.setParentId(1L); // Trùng với ID đang update
        when(categoryRepository.findById(id)).thenReturn(Optional.of(mockCategory));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.update(id, mockRequest));
        assertEquals("Danh mục không thể tự làm danh mục cha của chính nó!", ex.getMessage());
    }

    @Test
    @DisplayName("7. Delete: Thành công")
    void delete_Success() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.of(mockCategory));

        String result = categoryService.delete(id);

        assertTrue(result.contains("Đã xóa thành công"));
        verify(categoryRepository).delete(mockCategory);
    }

    @Test
    @DisplayName("8. Delete: Lỗi khi không tìm thấy ID")
    void delete_Fail_NotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.delete(99L));
    }
}