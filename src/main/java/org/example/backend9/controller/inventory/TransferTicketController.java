package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.TransferTicketRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.inventory.TransferTicketResponse;
import org.example.backend9.service.inventory.TransferTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/transfer-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class TransferTicketController {

    private final TransferTicketService ticketService;

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<TransferTicketResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", ticketService.getAll()));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TransferTicketResponse>> create(@RequestBody TransferTicketRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lập phiếu chuyển kho thành công", ticketService.create(request)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TransferTicketResponse>> update(@PathVariable Integer id, @RequestBody TransferTicketRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật phiếu thành công", ticketService.update(id, request)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        ticketService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa phiếu thành công", null));
    }

    @PutMapping("/process/{id}")
    public ResponseEntity<ApiResponse<TransferTicketResponse>> process(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã xuất hàng đi", ticketService.processTransfer(id)));
    }

    @PutMapping("/confirm/{id}")
    public ResponseEntity<ApiResponse<TransferTicketResponse>> confirm(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã nhận hàng thành công", ticketService.confirmReceipt(id)));
    }
}