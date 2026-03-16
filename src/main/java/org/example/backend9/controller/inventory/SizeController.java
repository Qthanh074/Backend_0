package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.SizeRequest;
import org.example.backend9.dto.response.inventory.SizeResponse;
import org.example.backend9.service.inventory.SizeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/sizes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class SizeController {
    private final SizeService sizeService;

    @GetMapping("/get-all")
    public ResponseEntity<List<SizeResponse>> getAll() {
        return ResponseEntity.ok(sizeService.getAll());
    }

    @PostMapping("/create")
    public ResponseEntity<SizeResponse> create(@RequestBody SizeRequest request) {
        return ResponseEntity.ok(sizeService.create(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SizeResponse> update(@PathVariable Long id, @RequestBody SizeRequest request) {
        return ResponseEntity.ok(sizeService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = sizeService.delete(id);
        return ResponseEntity.ok(message);
    }
}