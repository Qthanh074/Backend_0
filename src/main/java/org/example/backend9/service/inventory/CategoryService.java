package org.example.backend9.service.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.CategoryRequest;
import org.example.backend9.dto.response.inventory.CategoryResponse;
import org.example.backend9.entity.inventory.Category;
import org.example.backend9.repository.inventory.CategoryRepository;
import org.example.backend9.service.ExcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ExcelService excelService;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên danh mục này đã tồn tại!");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);

        // LUÔN LƯU VÀO EXCEL THEO YÊU CẦU CỦA THÀNH
        try {
            List<String> headers = Arrays.asList("ID", "Tên danh mục", "Mô tả", "Danh mục cha", "Trạng thái");
            List<List<Object>> data = Arrays.asList(
                    Arrays.asList(
                            saved.getId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getParent() != null ? saved.getParent().getName() : "Không có",
                            saved.getStatus().toString()
                    )
            );
            excelService.exportToExcel("Category_Export.xlsx", headers, data);
        } catch (Exception e) {
            System.err.println("Lỗi ghi file Excel: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public String delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục id: " + id));
        String name = category.getName();
        categoryRepository.delete(category);
        return "Đã xóa thành công danh mục: " + name;
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId() != null ? Long.valueOf(category.getId().toString()) : null)
                .name(category.getName())
                .description(category.getDescription())
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .status(category.getStatus())
                .build();
    }
}