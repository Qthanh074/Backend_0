package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.UnitRequest;
import org.example.backend9.dto.response.inventory.UnitResponse;
import org.example.backend9.entity.inventory.Unit;
import org.example.backend9.repository.inventory.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;

    public List<UnitResponse> getAll() {
        return unitRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UnitResponse create(UnitRequest request) {
        if (unitRepository.existsByName(request.getName())) {
            throw new RuntimeException("Đơn vị tính này đã tồn tại!");
        }

        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setDescription(request.getDescription());
        unit.setStatus(request.getStatus());
        unit.setIsBaseUnit(false);

        Unit saved = unitRepository.save(unit);

        return mapToResponse(saved);
    }

    @Transactional
    public UnitResponse update(Long id, UnitRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị id: " + id));

        // Kiểm tra xem tên mới có bị trùng với đơn vị khác đã có trong DB không
        if (!unit.getName().equals(request.getName()) && unitRepository.existsByName(request.getName())) {
            throw new RuntimeException("Đơn vị tính này đã tồn tại!");
        }

        unit.setName(request.getName());
        unit.setDescription(request.getDescription());
        unit.setStatus(request.getStatus());

        return mapToResponse(unitRepository.save(unit));
    }

    @Transactional
    public String delete(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị id: " + id));

        String unitName = unit.getName();
        unitRepository.delete(unit);

        return "Đã xóa thành công đơn vị tính: " + unitName;
    }

    private UnitResponse mapToResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId() != null ? Long.valueOf(unit.getId().toString()) : null)
                .name(unit.getName())
                .description(unit.getDescription())
                .status(unit.getStatus())
                .build();
    }
}