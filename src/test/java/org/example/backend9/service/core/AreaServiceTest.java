package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.AreaRequest;
import org.example.backend9.dto.response.core.AreaResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository; // Giả lập database

    @InjectMocks
    private AreaService areaService; // Đối tượng cần test

    private Area mockArea;
    private AreaRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu mẫu trước mỗi test case để tái sử dụng
        mockArea = new Area();
        mockArea.setId(1);
        mockArea.setCode("MB");
        mockArea.setName("Miền Bắc");
        mockArea.setDescription("Khu vực phía Bắc");
        mockArea.setStatus(EntityStatus.ACTIVE);

        mockRequest = new AreaRequest();
        mockRequest.setCode("MB");
        mockRequest.setName("Miền Bắc");
        mockRequest.setDescription("Khu vực phía Bắc");
    }

    @Test
    void getAllAreas_ShouldReturnListOfAreaResponse() {
        // Arrange (Chuẩn bị)
        when(areaRepository.findAll()).thenReturn(List.of(mockArea));

        // Act (Thực thi)
        List<AreaResponse> responses = areaService.getAllAreas();

        // Assert (Kiểm tra kết quả)
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("MB", responses.get(0).getCode());

        // Kiểm tra xem storeCount có bằng 0 khi stores bị null hay không
        assertEquals(0, responses.get(0).getStoreCount());
        verify(areaRepository, times(1)).findAll();
    }

    @Test
    void createArea_ShouldReturnSavedAreaResponse() {
        // Arrange
        when(areaRepository.save(any(Area.class))).thenReturn(mockArea);

        // Act
        AreaResponse response = areaService.createArea(mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals("MB", response.getCode());
        assertEquals(EntityStatus.ACTIVE, response.getStatus());
        verify(areaRepository, times(1)).save(any(Area.class));
    }

    @Test
    void updateArea_WhenIdExists_ShouldReturnUpdatedAreaResponse() {
        // Arrange
        when(areaRepository.findById(1)).thenReturn(Optional.of(mockArea));
        when(areaRepository.save(any(Area.class))).thenReturn(mockArea);

        // Act
        AreaResponse response = areaService.updateArea(1, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals("MB", response.getCode());
        verify(areaRepository, times(1)).findById(1);
        verify(areaRepository, times(1)).save(any(Area.class));
    }

    @Test
    void updateArea_WhenIdDoesNotExist_ShouldThrowException() {
        // Arrange
        when(areaRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> areaService.updateArea(99, mockRequest));

        assertEquals("Không tìm thấy khu vực", exception.getMessage());
        verify(areaRepository, never()).save(any(Area.class));
    }

    @Test
    void deleteArea_WhenIdExists_ShouldSetStatusToInactive() {
        // Arrange
        when(areaRepository.findById(1)).thenReturn(Optional.of(mockArea));
        when(areaRepository.save(any(Area.class))).thenReturn(mockArea);

        // Act
        areaService.deleteArea(1);

        // Assert
        assertEquals(EntityStatus.INACTIVE, mockArea.getStatus());
        verify(areaRepository, times(1)).findById(1);
        verify(areaRepository, times(1)).save(mockArea);
    }
}