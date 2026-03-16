package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.AreaRequest;
import org.example.backend9.dto.response.core.AreaResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AreaService {
    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    // Hàm map thủ công từ Entity sang Response DTO
    private AreaResponse mapToResponse(Area area) {
        AreaResponse res = new AreaResponse();
        res.setId(area.getId());
        res.setCode(area.getCode());
        res.setName(area.getName());
        res.setDescription(area.getDescription());
        res.setStatus(area.getStatus());

        // 👉 THÊM DÒNG NÀY: Đếm số cửa hàng từ List<Store> trong Entity Area
        // Giả sử trong Entity Area bạn đã đặt: @OneToMany(mappedBy = "area") List<Store> stores;
        if (area.getStores() != null) {
            res.setStoreCount(area.getStores().size());
        } else {
            res.setStoreCount(0);
        }

        return res;
    }
    @Transactional(readOnly = true)
    public List<AreaResponse> getAllAreas() {
        return areaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AreaResponse createArea(AreaRequest request) {
        Area area = new Area();
        area.setCode(request.getCode().toUpperCase());
        area.setName(request.getName());
        area.setDescription(request.getDescription());
        area.setStatus(EntityStatus.ACTIVE);
        return mapToResponse(areaRepository.save(area));
    }

    @Transactional
    public AreaResponse updateArea(Integer id, AreaRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));

        area.setCode(request.getCode().toUpperCase());
        area.setName(request.getName());
        area.setDescription(request.getDescription());


        if (request.getStatus() != null) {
            area.setStatus(request.getStatus());
        }

        return mapToResponse(areaRepository.save(area));
    }
    @Transactional
    public void deleteArea(Integer id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));
        area.setStatus(EntityStatus.INACTIVE);
        areaRepository.save(area);
    }
}