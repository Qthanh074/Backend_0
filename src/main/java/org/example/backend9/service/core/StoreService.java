package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.StoreRequest;
import org.example.backend9.dto.response.core.StoreResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final AreaRepository areaRepository;

    public StoreService(StoreRepository storeRepository, AreaRepository areaRepository) {
        this.storeRepository = storeRepository;
        this.areaRepository = areaRepository;
    }

    private StoreResponse mapToResponse(Store store) {
        StoreResponse res = new StoreResponse();
        res.setId(store.getId());
        res.setCode(store.getCode());
        res.setName(store.getName());
        res.setAddress(store.getAddress());
        res.setPhone(store.getPhone());
        res.setEmail(store.getEmail());
        res.setStatus(store.getStatus());
        if (store.getArea() != null) {
            res.setAreaName(store.getArea().getName());
        }
        return res;
    }

    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        Store store = new Store();
        store.setCode("CH" + System.currentTimeMillis() % 10000);
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setArea(area);
        store.setStatus(EntityStatus.ACTIVE);

        return mapToResponse(storeRepository.save(store));
    }

    @Transactional
    public StoreResponse updateStore(Integer id, StoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setEmail(request.getEmail());
        store.setArea(area);
        if (request.getStatus() != null) {
            store.setStatus(request.getStatus());
        }
        return mapToResponse(storeRepository.save(store));
    }
    @Transactional
    public void deleteStore(Integer id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));

        // Chuyển trạng thái sang Ngừng hoạt động thay vì xóa hẳn
        store.setStatus(EntityStatus.INACTIVE);
        storeRepository.save(store);
    }
}