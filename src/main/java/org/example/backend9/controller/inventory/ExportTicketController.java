package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ExportTicketRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.inventory.ExportTicketResponse;
import org.example.backend9.service.inventory.ExportTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/export-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class ExportTicketController {

    private final ExportTicketService exportTicketService;

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<ExportTicketResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy danh sách phiếu xuất thành công", exportTicketService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExportTicketResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy chi tiết phiếu xuất thành công", exportTicketService.getById(id))
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ExportTicketResponse>> create(@RequestBody ExportTicketRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lập phiếu xuất thành công", exportTicketService.create(request))
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ExportTicketResponse>> update(
            @PathVariable Integer id,
            @RequestBody ExportTicketRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cập nhật phiếu xuất thành công", exportTicketService.update(id, request))
        );
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<ExportTicketResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Hủy phiếu xuất thành công", exportTicketService.cancelTicket(id))
        );
    }
}