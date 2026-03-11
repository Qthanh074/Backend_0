package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ColorRequest;
import org.example.backend9.dto.response.inventory.ColorResponse;
import org.example.backend9.entity.inventory.Color;
import org.example.backend9.repository.inventory.ColorRepository;
import org.example.backend9.service.ExcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorService {
    private final ColorRepository colorRepository;
    private final ExcelService excelService;

    public List<ColorResponse> getAll() {
        return colorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ColorResponse create(ColorRequest request) {
        if (colorRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên màu đã tồn tại!");
        }

        Color color = new Color();
        color.setName(request.getName());
        color.setHexCode(request.getHexCode());
        color.setStatus(request.getStatus());

        Color saved = colorRepository.save(color);

        try {
            List<String> headers = Arrays.asList("ID", "Tên màu", "Mã Hex", "Trạng thái");
            List<List<Object>> data = Arrays.asList(
                    Arrays.asList(
                            saved.getId(),
                            saved.getName(),
                            saved.getHexCode(),
                            saved.getStatus().toString()
                    )
            );
            excelService.exportToExcel("Color_Export.xlsx", headers, data);
        } catch (Exception e) {
            System.err.println("Lỗi ghi file Excel: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public ColorResponse update(Long id, ColorRequest request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc id: " + id));

        color.setName(request.getName());
        color.setHexCode(request.getHexCode());
        color.setStatus(request.getStatus());

        return mapToResponse(colorRepository.save(color));
    }

    public void delete(Long id) {
        colorRepository.deleteById(id);
    }

    private ColorResponse mapToResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId() != null ? Long.valueOf(color.getId().toString()) : null)
                .name(color.getName())
                .hexCode(color.getHexCode())
                .status(color.getStatus())
                .build();
    }
}