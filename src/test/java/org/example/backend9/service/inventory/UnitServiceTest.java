package org.example.backend9.service.inventory;

import org.example.backend9.dto.request.inventory.UnitRequest;
import org.example.backend9.dto.response.inventory.UnitResponse;
import org.example.backend9.entity.inventory.Unit;
import org.example.backend9.enums.EntityStatus; // Kiểm tra đúng package Enum của bạn
import org.example.backend9.repository.inventory.UnitRepository;
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
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitService unitService;

    private Unit mockUnit;
    private UnitRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Giả lập dữ liệu Unit mẫu
        mockUnit = new Unit();
        mockUnit.setId(1); // Nếu Entity dùng Integer, hãy để 1. Nếu Long hãy để 1L
        mockUnit.setName("Cái");
        mockUnit.setDescription("Đơn vị đếm chiếc");
        mockUnit.setStatus(EntityStatus.ACTIVE);

        // Giả lập Request mẫu
        mockRequest = new UnitRequest();
        mockRequest.setName("Hộp");
        mockRequest.setDescription("Đơn vị hộp");
        mockRequest.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("1. GetAll: Lấy danh sách đơn vị tính thành công")
    void getAll_Success() {
        when(unitRepository.findAll()).thenReturn(List.of(mockUnit));

        List<UnitResponse> result = unitService.getAll();

        assertFalse(result.isEmpty());
        assertEquals("Cái", result.get(0).getName());
    }

    @Test
    @DisplayName("2. Create: Tạo mới thành công")
    void create_Success() {
        when(unitRepository.existsByName("Hộp")).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenReturn(mockUnit);

        UnitResponse res = unitService.create(mockRequest);

        assertNotNull(res);
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    @DisplayName("3. Create: Thất bại do trùng tên đơn vị")
    void create_Fail_DuplicateName() {
        when(unitRepository.existsByName("Hộp")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> unitService.create(mockRequest));
        assertEquals("Đơn vị tính này đã tồn tại!", ex.getMessage());
    }

    @Test
    @DisplayName("4. Update: Cập nhật thành công")
    void update_Success() {
        Long id = 1L; // Nhớ ép kiểu Long nếu ID của bạn là Long
        when(unitRepository.findById(id)).thenReturn(Optional.of(mockUnit));
        when(unitRepository.existsByName("Hộp")).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenReturn(mockUnit);

        UnitResponse res = unitService.update(id, mockRequest);

        assertNotNull(res);
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    @DisplayName("5. Update: Thất bại khi tên mới trùng với đơn vị khác")
    void update_Fail_NameExists() {
        Long id = 1L;
        when(unitRepository.findById(id)).thenReturn(Optional.of(mockUnit));
        when(unitRepository.existsByName("Hộp")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> unitService.update(id, mockRequest));
    }

    @Test
    @DisplayName("6. Delete: Xóa thành công")
    void delete_Success() {
        Long id = 1L;
        when(unitRepository.findById(id)).thenReturn(Optional.of(mockUnit));

        String result = unitService.delete(id);

        assertTrue(result.contains("Đã xóa thành công"));
        verify(unitRepository).delete(mockUnit);
    }

    @Test
    @DisplayName("7. Delete: Thất bại khi không tìm thấy đơn vị")
    void delete_Fail_NotFound() {
        Long id = 99L;
        when(unitRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> unitService.delete(id));
    }
}