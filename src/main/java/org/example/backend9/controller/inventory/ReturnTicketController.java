package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ReturnTicketRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.inventory.ReturnTicketResponse;
import org.example.backend9.service.inventory.ReturnTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// 🟢 ĐÃ ĐỔI ĐƯỜNG DẪN KHỚP 100% VỚI REACT 🟢
@RequestMapping("/api/inventory/return-tickets")
@RequiredArgsConstructor
public class ReturnTicketController {

    private final ReturnTicketService returnService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnTicketResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", returnService.getAll()));
    }

    // 🟢 BỔ SUNG API XEM CHI TIẾT (Tránh lỗi khi bấm icon Con mắt) 🟢
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", returnService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> create(@RequestBody ReturnTicketRequest request) {
        // Biến returnType (Khách trả hay Trả NCC) đã được React truyền sẵn trong request rồi!
        return ResponseEntity.ok(new ApiResponse<>(true, "Tạo phiếu trả thành công", returnService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> update(@PathVariable Integer id, @RequestBody ReturnTicketRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật phiếu thành công", returnService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        returnService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa phiếu trả thành công", null));
    }
}