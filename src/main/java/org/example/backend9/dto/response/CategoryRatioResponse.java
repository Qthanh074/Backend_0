package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRatioResponse {
    private String categoryName;
    private Long productCount;
    private Double percentage;
}