package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.InventoryCheckRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.inventory.InventoryCheckResponse;
import org.example.backend9.service.inventory.InventoryCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/inventory-checks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
public class InventoryCheckController {

    private final InventoryCheckService checkService;

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<InventoryCheckResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", checkService.getAll()));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InventoryCheckResponse>> create(@RequestBody InventoryCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lập phiếu kiểm kho thành công", checkService.create(request)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<InventoryCheckResponse>> update(@PathVariable Integer id, @RequestBody InventoryCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật phiếu kiểm kho thành công", checkService.update(id, request)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", checkService.delete(id)));
    }

    @PutMapping("/balance/{id}")
    public ResponseEntity<ApiResponse<InventoryCheckResponse>> balance(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cân bằng kho thành công", checkService.balanceInventory(id)));
    }
}