package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.ColorRequest;
import org.example.backend9.dto.response.inventory.ColorResponse;
import org.example.backend9.entity.inventory.Color;
import org.example.backend9.enums.EntityStatus; // Nhớ check đúng package Enum của bạn
import org.example.backend9.repository.inventory.ColorRepository;
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
class ColorServiceTest {

    @Mock
    private ColorRepository colorRepository;

    @InjectMocks
    private ColorService colorService;

    private Color mockColor;
    private ColorRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Giả lập dữ liệu mẫu
        mockColor = new Color();
        mockColor.setId(1);
        mockColor.setName("Đỏ");
        mockColor.setHexCode("#FF0000");
        mockColor.setStatus(EntityStatus.ACTIVE);

        mockRequest = new ColorRequest();
        mockRequest.setName("Xanh");
        mockRequest.setHexCode("#0000FF");
        mockRequest.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("1. GetAll: Trả về danh sách màu sắc")
    void getAll_Success() {
        when(colorRepository.findAll()).thenReturn(List.of(mockColor));

        List<ColorResponse> result = colorService.getAll();

        assertEquals(1, result.size());
        assertEquals("Đỏ", result.get(0).getName());
    }

    @Test
    @DisplayName("2. Create: Thành công")
    void create_Success() {
        when(colorRepository.existsByName("Xanh")).thenReturn(false);
        when(colorRepository.save(any(Color.class))).thenAnswer(i -> i.getArgument(0));

        ColorResponse res = colorService.create(mockRequest);

        assertNotNull(res);
        assertEquals("Xanh", res.getName());
        verify(colorRepository).save(any(Color.class));
    }

    @Test
    @DisplayName("3. Create: Thất bại do trùng tên màu")
    void create_Fail_DuplicateName() {
        when(colorRepository.existsByName("Xanh")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> colorService.create(mockRequest));
        assertEquals("Tên màu đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("4. Update: Thành công")
    void update_Success() {
        Long id = 1L;
        when(colorRepository.findById(id)).thenReturn(Optional.of(mockColor));
        when(colorRepository.existsByName("Xanh")).thenReturn(false);
        when(colorRepository.save(any(Color.class))).thenAnswer(i -> i.getArgument(0));

        ColorResponse res = colorService.update(id, mockRequest);

        assertEquals("Xanh", res.getName());
        assertEquals("#0000FF", res.getHexCode());
    }

    @Test
    @DisplayName("5. Update: Lỗi khi đổi tên trùng với màu khác")
    void update_Fail_NameExists() {
        Long id = 1L;
        // Giả sử màu hiện tại là "Đỏ", muốn đổi sang "Xanh" nhưng "Xanh" đã có trong DB rồi
        when(colorRepository.findById(id)).thenReturn(Optional.of(mockColor));
        when(colorRepository.existsByName("Xanh")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> colorService.update(id, mockRequest));
        assertEquals("Tên màu đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("6. Delete: Thành công")
    void delete_Success() {
        Long id = 1L;
        when(colorRepository.findById(id)).thenReturn(Optional.of(mockColor));

        String result = colorService.delete(id);

        assertTrue(result.contains("Đã xóa thành công"));
        verify(colorRepository).delete(mockColor);
    }

    @Test
    @DisplayName("7. Delete: Lỗi khi không tìm thấy ID")
    void delete_Fail_NotFound() {
        Long id = 99L;
        when(colorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> colorService.delete(id));
    }
}