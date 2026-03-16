package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.SizeRequest;
import org.example.backend9.dto.response.inventory.SizeResponse;
import org.example.backend9.entity.inventory.Size;
import org.example.backend9.repository.inventory.SizeRepository;
import org.example.backend9.service.GoogleSheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SizeService {
    private final SizeRepository sizeRepository;
    private final GoogleSheetService googleSheetService;

    public List<SizeResponse> getAll() {
        return sizeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SizeResponse create(SizeRequest request) {
        if (sizeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Kích thước này đã tồn tại!");
        }

        Size size = new Size();
        size.setName(request.getName());
        size.setDescription(request.getDescription());
        size.setStatus(request.getStatus());

        Size saved = sizeRepository.save(size);

        try {
            List<Object> rowData = Arrays.asList(
                    saved.getId().toString(),
                    saved.getName(),
                    saved.getDescription() != null ? saved.getDescription() : "",
                    saved.getStatus() != null ? saved.getStatus().name() : "ACTIVE",
                    LocalDateTime.now().toString()
            );

            googleSheetService.appendRowToSheet("Size", rowData);
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ Google Sheets (Size): " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public SizeResponse update(Long id, SizeRequest request) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước id: " + id));

        size.setName(request.getName());
        size.setDescription(request.getDescription());
        size.setStatus(request.getStatus());

        return mapToResponse(sizeRepository.save(size));
    }

    @Transactional
    public String delete(Long id) {
        Size size = sizeRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước id: " + id));
        String name = size.getName();
        sizeRepository.delete(size);
        return "Đã xóa thành công kích thước: " + name;
    }

    private SizeResponse mapToResponse(Size size) {
        return SizeResponse.builder()
                .id(size.getId() != null ? Long.valueOf(size.getId().toString()) : null)
                .name(size.getName())
                .description(size.getDescription())
                .status(size.getStatus())
                .build();
    }
}