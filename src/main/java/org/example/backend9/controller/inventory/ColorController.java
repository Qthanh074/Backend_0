package org.example.backend9.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.example.backend9.dto.request.inventory.ColorRequest;
import org.example.backend9.dto.response.inventory.ColorResponse;
import org.example.backend9.service.inventory.ColorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/colors")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ColorController {
    private final ColorService colorService;

    @GetMapping
    public ResponseEntity<List<ColorResponse>> getAll() {
        return ResponseEntity.ok(colorService.getAll());
    }

    @PostMapping
    public ResponseEntity<ColorResponse> create(@RequestBody ColorRequest request) {
        return ResponseEntity.ok(colorService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorResponse> update(@PathVariable Long id, @RequestBody ColorRequest request) {
        return ResponseEntity.ok(colorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = colorService.delete(id);
        return ResponseEntity.ok(message);
    }
}