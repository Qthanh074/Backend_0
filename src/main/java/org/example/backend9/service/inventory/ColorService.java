package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ColorRequest;
import org.example.backend9.dto.response.inventory.ColorResponse;
import org.example.backend9.entity.inventory.Color;
import org.example.backend9.repository.inventory.ColorRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorService {
    private final ColorRepository colorRepository;
    private final GoogleSheetService googleSheetService;

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
            List<Object> rowData = Arrays.asList(
                    saved.getId().toString(),
                    saved.getName(),
                    saved.getHexCode(),
                    saved.getStatus() != null ? saved.getStatus().name() : "ACTIVE",
                    LocalDateTime.now().toString()
            );
            googleSheetService.appendRowToSheet("Color", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (Color): " + e.getMessage());
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

        Color updated = colorRepository.save(color);

        return mapToResponse(updated);
    }

    @Transactional
    public String delete(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc id: " + id));
        String name = color.getName();
        colorRepository.delete(color);
        return "Đã xóa thành công màu: " + name;
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