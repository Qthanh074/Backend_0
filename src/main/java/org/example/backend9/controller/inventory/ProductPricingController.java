package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.PriceSetupRequest;
import org.example.backend9.dto.response.inventory.PriceResponse;
import org.example.backend9.service.inventory.ProductPricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/pricings")
@RequiredArgsConstructor
public class ProductPricingController {
    private final ProductPricingService pricingService;

    @GetMapping
    public ResponseEntity<List<PriceResponse>> getAll() {
        return ResponseEntity.ok(pricingService.getAll());
    }

    @PostMapping("/setup") // Nút "Thiết lập giá"
    public ResponseEntity<PriceResponse> setup(@RequestBody PriceSetupRequest request) {
        return ResponseEntity.ok(pricingService.setupPrice(request));
    }

    @PutMapping("/{id}/approve") // Nút "Duyệt giá mới"
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        pricingService.approvePrice(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}") // Nút "Xóa"
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(pricingService.delete(id));
    }
}