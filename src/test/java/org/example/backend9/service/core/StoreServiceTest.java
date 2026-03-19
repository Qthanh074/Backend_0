package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.StoreRequest;
import org.example.backend9.dto.response.core.StoreResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private StoreService storeService;

    private Store mockStore;
    private Area mockArea;
    private StoreRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockArea = new Area();
        mockArea.setId(1);
        mockArea.setName("Miền Bắc");

        mockStore = new Store();
        mockStore.setId(1);
        mockStore.setCode("S001");
        mockStore.setName("Cửa hàng Hà Nội");
        mockStore.setStatus(EntityStatus.ACTIVE);
        mockStore.setArea(mockArea);

        mockRequest = new StoreRequest();
        mockRequest.setName("S001-UPDATED");
        mockRequest.setName("Cửa hàng Cầu Giấy");
        mockRequest.setAreaId(1);
    }

    // --- TEST CREATE ---
    @Test
    void createStore_ShouldReturnResponse_WhenSuccess() {
        when(areaRepository.findById(1)).thenReturn(Optional.of(mockArea));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        StoreResponse response = storeService.createStore(mockRequest);

        assertNotNull(response);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    // --- TEST UPDATE ---
    @Test
    void updateStore_ShouldUpdateData_WhenIdExists() {
        // Giả lập tìm thấy Store cũ và Area mới
        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));
        when(areaRepository.findById(1)).thenReturn(Optional.of(mockArea));
        when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

        StoreResponse response = storeService.updateStore(1, mockRequest);

        assertNotNull(response);
        verify(storeRepository).save(mockStore);
    }

    @Test
    void updateStore_ShouldThrowException_WhenStoreNotFound() {
        when(storeRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> storeService.updateStore(99, mockRequest));
        verify(storeRepository, never()).save(any());
    }

    // --- TEST DELETE ---
    @Test
    void deleteStore_ShouldSetStatusToInactive() {
        when(storeRepository.findById(1)).thenReturn(Optional.of(mockStore));

        storeService.deleteStore(1);

        // Kiểm tra xem status đã chuyển thành INACTIVE chưa
        assertEquals(EntityStatus.INACTIVE, mockStore.getStatus());
        verify(storeRepository).save(mockStore);
    }
}