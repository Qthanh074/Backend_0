package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.UnitRequest;
import org.example.backend9.dto.response.inventory.UnitResponse;
import org.example.backend9.service.inventory.UnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/units")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class UnitController {
    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<List<UnitResponse>> getAll() {
        return ResponseEntity.ok(unitService.getAll());
    }

    @PostMapping
    public ResponseEntity<UnitResponse> create(@RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitResponse> update(@PathVariable Long id, @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = unitService.delete(id);
        return ResponseEntity.ok(message);
    }
}