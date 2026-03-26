package org.example.backend9.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> { // <T> là kiểu dữ liệu đại diện
    private boolean success;
    private String message;
    private T data; // 🟢 ĐỔI TỪ Object SANG T ĐỂ ĐỒNG BỘ
}