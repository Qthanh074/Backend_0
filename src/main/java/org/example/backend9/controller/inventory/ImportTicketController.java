package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ImportTicketRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.inventory.ImportTicketResponse;
import org.example.backend9.service.inventory.ImportTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/import-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class ImportTicketController {

    private final ImportTicketService importTicketService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImportTicketResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy danh sách phiếu nhập thành công", importTicketService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportTicketResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lấy chi tiết phiếu nhập thành công", importTicketService.getById(id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportTicketResponse>> create(@RequestBody ImportTicketRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lập phiếu nhập thành công", importTicketService.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportTicketResponse>> update(
            @PathVariable Integer id,
            @RequestBody ImportTicketRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cập nhật phiếu nhập thành công", importTicketService.update(id, request))
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ImportTicketResponse>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Hủy phiếu nhập thành công", importTicketService.cancelTicket(id))
        );
    }
}