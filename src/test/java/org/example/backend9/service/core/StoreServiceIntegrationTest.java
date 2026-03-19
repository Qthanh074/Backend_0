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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoreServiceIntegrationTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AreaRepository areaRepository;

    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    private Area savedArea;

    @BeforeEach
    void setUp() {
        // Cửa hàng bắt buộc phải thuộc về 1 Khu vực, nên ta phải tạo Khu vực trước
        Area area = new Area();
        area.setCode("MB_TEST");
        area.setName("Miền Bắc Test");
        area.setStatus(EntityStatus.ACTIVE);
        savedArea = areaRepository.save(area);
    }

    @Test
    void createStore_Success_ShouldSaveToDatabase() {
        StoreRequest request = new StoreRequest();
        request.setName("Cửa hàng Cầu Giấy");
        request.setAddress("123 Xuân Thủy");
        request.setPhone("0987654321");
        request.setEmail("caugiay@store.com");
        request.setAreaId(savedArea.getId());

        StoreResponse response = storeService.createStore(request);

        assertNotNull(response.getId());
        assertTrue(response.getCode().startsWith("CH")); // Test logic sinh mã tự động
        assertEquals("Cửa hàng Cầu Giấy", response.getName());
        assertEquals("Miền Bắc Test", response.getAreaName());

        Store dbStore = storeRepository.findById(response.getId()).orElseThrow();
        assertEquals(EntityStatus.ACTIVE, dbStore.getStatus());
    }

    @Test
    void createStore_WhenAreaNotFound_ShouldThrowException() {
        StoreRequest request = new StoreRequest();
        request.setAreaId(99999); // ID Khu vực ảo

        RuntimeException ex = assertThrows(RuntimeException.class, () -> storeService.createStore(request));
        assertEquals("Khu vực không tồn tại", ex.getMessage());
    }

    @Test
    void getAllStores_ShouldReturnList() {
        Store store = new Store();
        store.setCode("CH_TEST");
        store.setName("Store Test");
        store.setArea(savedArea);
        store.setStatus(EntityStatus.ACTIVE);
        storeRepository.save(store);

        List<StoreResponse> list = storeService.getAllStores();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.getName().equals("Store Test")));
    }

    @Test
    void updateStore_Success_ShouldUpdateData() {
        Store store = new Store();
        store.setCode("CH_OLD");
        store.setName("Tên Cũ");
        store.setArea(savedArea);
        store.setStatus(EntityStatus.ACTIVE);
        store = storeRepository.save(store);

        StoreRequest request = new StoreRequest();
        request.setName("Tên Mới");
        request.setAreaId(savedArea.getId());
        request.setStatus(EntityStatus.INACTIVE);

        StoreResponse response = storeService.updateStore(store.getId(), request);

        assertEquals("Tên Mới", response.getName());
        assertEquals(EntityStatus.INACTIVE, response.getStatus());
    }

    @Test
    void deleteStore_ShouldSetStatusToInactive() {
        Store store = new Store();
        store.setCode("CH_DEL");
        store.setName("Sắp bị xóa");
        store.setArea(savedArea);
        store.setStatus(EntityStatus.ACTIVE);
        store = storeRepository.save(store);

        storeService.deleteStore(store.getId());

        Store deletedStore = storeRepository.findById(store.getId()).orElseThrow();
        assertEquals(EntityStatus.INACTIVE, deletedStore.getStatus(), "Chỉ xóa mềm (Inactive), không xóa cứng");
    }
}