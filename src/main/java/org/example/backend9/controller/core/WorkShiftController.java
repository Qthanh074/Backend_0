package org.example.backend9.controller.core;

import jakarta.validation.Valid;
import org.example.backend9.dto.request.core.WorkShiftRequest;
import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.response.core.WorkShiftResponse;
import org.example.backend9.service.core.WorkShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/work-shifts")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    // Admin và Manager xem được
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<WorkShiftResponse>>> getAllShifts() {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Lấy danh sách ca làm việc thành công",
                        workShiftService.getAllShifts())
        );
    }

    // Nhân viên check-in
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    public ResponseEntity<ApiResponse<WorkShiftResponse>> checkIn(
            @Valid @RequestBody WorkShiftRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Check-in thành công",
                        workShiftService.checkIn(request))
        );
    }

    // Nhân viên check-out
    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    public ResponseEntity<ApiResponse<WorkShiftResponse>> checkOut(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Check-out thành công",
                        workShiftService.checkOut(id))
        );
    }
}