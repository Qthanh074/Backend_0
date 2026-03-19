package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.WorkShiftRequest;
import org.example.backend9.dto.response.core.WorkShiftResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.WorkShift;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.WorkShiftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkShiftServiceTest {

    @Mock private WorkShiftRepository workShiftRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private StoreRepository storeRepository; // 👉 QUAN TRỌNG: Thêm dòng này để fix lỗi Null

    @InjectMocks private WorkShiftService workShiftService;

    @Test
    void checkIn_ShouldReturnResponseWithStartTime() {
        // Chuẩn bị dữ liệu mẫu
        WorkShiftRequest request = new WorkShiftRequest();
        request.setEmployeeId(1);
        request.setStoreId(1);

        Employee emp = new Employee();
        emp.setId(1);
        Store store = new Store();
        store.setId(1);

        WorkShift shift = new WorkShift();
        shift.setStartTime(LocalTime.from(LocalDateTime.now()));
        shift.setEmployee(emp);
        shift.setStore(store);

        // Giả lập các bước tìm kiếm trong service
        when(employeeRepository.findById(1)).thenReturn(Optional.of(emp));
        when(storeRepository.findById(1)).thenReturn(Optional.of(store));
        when(workShiftRepository.save(any())).thenReturn(shift);

        // Thực thi
        WorkShiftResponse result = workShiftService.checkIn(request);

        // Kiểm tra
        assertNotNull(result.getStartTime());
        verify(workShiftRepository).save(any());
    }

    @Test
    void checkOut_ShouldSetEndTimeAndReturnResponse() {
        WorkShift shift = new WorkShift();
        shift.setId(1);
        shift.setStartTime(LocalTime.from(LocalDateTime.now().minusHours(8)));

        when(workShiftRepository.findById(1)).thenReturn(Optional.of(shift));
        when(workShiftRepository.save(any())).thenReturn(shift);

        WorkShiftResponse result = workShiftService.checkOut(1);

        assertNotNull(result.getEndTime());
        verify(workShiftRepository).save(any());
    }
}