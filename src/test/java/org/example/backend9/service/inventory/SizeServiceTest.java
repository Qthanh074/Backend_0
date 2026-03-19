package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.SizeRequest;
import org.example.backend9.dto.response.inventory.SizeResponse;
import org.example.backend9.entity.inventory.Size;
import org.example.backend9.enums.EntityStatus; // Kiểm tra lại đúng package Enum của bạn
import org.example.backend9.repository.inventory.SizeRepository;
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

@ExtendWith(MockitoExtension.class)
class SizeServiceTest {

    @Mock
    private SizeRepository sizeRepository;

    @InjectMocks
    private SizeService sizeService;

    private Size mockSize;
    private SizeRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Giả lập dữ liệu mẫu
        mockSize = new Size();
        mockSize.setId(1); // Nếu Entity dùng Integer, hãy đổi thành 1
        mockSize.setName("XL");
        mockSize.setDescription("Kích thước cực đại");
        mockSize.setStatus(EntityStatus.ACTIVE);

        mockRequest = new SizeRequest();
        mockRequest.setName("L");
        mockRequest.setDescription("Kích thước lớn");
        mockRequest.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("1. GetAll: Trả về danh sách kích thước")
    void getAll_Success() {
        when(sizeRepository.findAll()).thenReturn(List.of(mockSize));

        List<SizeResponse> result = sizeService.getAll();

        assertEquals(1, result.size());
        assertEquals("XL", result.get(0).getName());
    }

    @Test
    @DisplayName("2. Create: Thành công")
    void create_Success() {
        when(sizeRepository.existsByName("L")).thenReturn(false);
        when(sizeRepository.save(any(Size.class))).thenAnswer(i -> i.getArgument(0));

        SizeResponse res = sizeService.create(mockRequest);

        assertNotNull(res);
        assertEquals("L", res.getName());
        verify(sizeRepository).save(any(Size.class));
    }

    @Test
    @DisplayName("3. Create: Thất bại do trùng tên")
    void create_Fail_DuplicateName() {
        when(sizeRepository.existsByName("L")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sizeService.create(mockRequest));
        assertEquals("Kích thước này đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("4. Update: Thành công")
    void update_Success() {
        Long id = 1L; // Nhớ check kiểu dữ liệu Long/Integer
        when(sizeRepository.findById(id)).thenReturn(Optional.of(mockSize));
        when(sizeRepository.existsByName("L")).thenReturn(false);
        when(sizeRepository.save(any(Size.class))).thenAnswer(i -> i.getArgument(0));

        SizeResponse res = sizeService.update(id, mockRequest);

        assertEquals("L", res.getName());
        verify(sizeRepository).save(any(Size.class));
    }

    @Test
    @DisplayName("5. Update: Thất bại khi tên mới trùng với Size khác")
    void update_Fail_NameExists() {
        Long id = 1L;
        when(sizeRepository.findById(id)).thenReturn(Optional.of(mockSize));
        when(sizeRepository.existsByName("L")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sizeService.update(id, mockRequest));
        assertEquals("Kích thước này đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("6. Delete: Thành công")
    void delete_Success() {
        Long id = 1L;
        when(sizeRepository.findById(id)).thenReturn(Optional.of(mockSize));

        String result = sizeService.delete(id);

        assertTrue(result.contains("Đã xóa thành công"));
        verify(sizeRepository).delete(mockSize);
    }

    @Test
    @DisplayName("7. Delete: Thất bại khi không tìm thấy ID")
    void delete_Fail_NotFound() {
        Long id = 99L;
        when(sizeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sizeService.delete(id));
    }
}