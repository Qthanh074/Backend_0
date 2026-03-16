package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.WorkShiftRequest;
import org.example.backend9.dto.response.core.WorkShiftResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.WorkShift;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.backend9.service.core.WorkShiftService;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkShiftService {
    private final WorkShiftRepository workShiftRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;

    public WorkShiftService(WorkShiftRepository workShiftRepository, StoreRepository storeRepository, EmployeeRepository employeeRepository) {
        this.workShiftRepository = workShiftRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
    }

    private WorkShiftResponse mapToResponse(WorkShift entity) {
        WorkShiftResponse res = new WorkShiftResponse();
        res.setId(entity.getId());
        res.setShiftDate(entity.getShiftDate());
        res.setStartTime(entity.getStartTime());
        res.setEndTime(entity.getEndTime());
        res.setNotes(entity.getNotes());

        // 👉 QUAN TRỌNG: Gán tên từ Entity sang DTO
        if (entity.getEmployee() != null) {
            res.setEmployeeName(entity.getEmployee().getFullName());
        }
        if (entity.getStore() != null) {
            res.setStoreName(entity.getStore().getName());
        }

        return res;
    }

    public List<WorkShiftResponse> getAllShifts() {
        return workShiftRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkShiftResponse checkIn(WorkShiftRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        WorkShift shift = new WorkShift();
        shift.setStore(store);
        shift.setEmployee(emp);
        shift.setShiftDate(request.getShiftDate());
        shift.setStartTime(request.getStartTime());
        shift.setNotes(request.getNotes());

        return mapToResponse(workShiftRepository.save(shift));
    }

    @Transactional
    public WorkShiftResponse checkOut(Integer id) {
        WorkShift shift = workShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));
        shift.setEndTime(LocalTime.now());
        return mapToResponse(workShiftRepository.save(shift));
    }
}