package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.UnitRequest;
import org.example.backend9.dto.response.inventory.UnitResponse;
import org.example.backend9.entity.inventory.Unit;
import org.example.backend9.repository.inventory.UnitRepository;
import org.example.backend9.service.ExcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;
    private final ExcelService excelService;

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

        try {
            List<String> headers = Arrays.asList("ID", "Tên đơn vị", "Mô tả", "Trạng thái");
            List<List<Object>> data = Arrays.asList(
                    Arrays.asList(
                            saved.getId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getStatus() != null ? saved.getStatus().toString() : ""
                    )
            );
            // ĐÃ SỬA: Thứ tự đúng là (data, headers, fileName)
            excelService.exportToExcel("Unit_Export.xlsx", headers, data);
        } catch (Exception e) {
            System.err.println("Lỗi ghi file Excel: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public UnitResponse update(Long id, UnitRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị id: " + id));

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