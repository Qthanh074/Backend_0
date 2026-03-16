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
@RequestMapping("/api/inventory/returns")
@RequiredArgsConstructor
public class ReturnTicketController {

    private final ReturnTicketService returnService;

    @GetMapping("/customer/get-all")
    public ResponseEntity<ApiResponse<List<ReturnTicketResponse>>> getCustomerReturns() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", returnService.getByReturnType("CUSTOMER_RETURN")));
    }

    @PostMapping("/customer/create")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> createCustomerReturn(@RequestBody ReturnTicketRequest request) {
        request.setReturnType("CUSTOMER_RETURN");
        return ResponseEntity.ok(new ApiResponse<>(true, "Tạo phiếu khách trả thành công", returnService.create(request)));
    }

    @PutMapping("/customer/update/{id}")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> updateCustomerReturn(@PathVariable Integer id, @RequestBody ReturnTicketRequest request) {
        request.setReturnType("CUSTOMER_RETURN");
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật phiếu khách trả thành công", returnService.update(id, request)));
    }

    @DeleteMapping("/customer/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerReturn(@PathVariable Integer id) {
        returnService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa phiếu khách trả thành công", null));
    }

    @GetMapping("/supplier/get-all")
    public ResponseEntity<ApiResponse<List<ReturnTicketResponse>>> getSupplierReturns() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Thành công", returnService.getByReturnType("SUPPLIER_RETURN")));
    }

    @PostMapping("/supplier/create")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> createSupplierReturn(@RequestBody ReturnTicketRequest request) {
        request.setReturnType("SUPPLIER_RETURN");
        return ResponseEntity.ok(new ApiResponse<>(true, "Tạo phiếu trả NCC thành công", returnService.create(request)));
    }

    @PutMapping("/supplier/update/{id}")
    public ResponseEntity<ApiResponse<ReturnTicketResponse>> updateSupplierReturn(@PathVariable Integer id, @RequestBody ReturnTicketRequest request) {
        request.setReturnType("SUPPLIER_RETURN");
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật phiếu trả NCC thành công", returnService.update(id, request)));
    }

    @DeleteMapping("/supplier/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplierReturn(@PathVariable Integer id) {
        returnService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Xóa phiếu trả NCC thành công", null));
    }
}