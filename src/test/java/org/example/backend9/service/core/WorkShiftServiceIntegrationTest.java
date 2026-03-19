package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.WorkShiftRequest;
import org.example.backend9.dto.response.core.WorkShiftResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.entity.core.WorkShift;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.AreaRepository;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.example.backend9.repository.core.WorkShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkShiftServiceIntegrationTest {

    @Autowired
    private WorkShiftService workShiftService;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AreaRepository areaRepository;

    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    private Store savedStore;
    private Employee savedEmployee;

    @BeforeEach
    void setUp() {
        // 1. Tạo Area
        Area area = new Area();
        area.setCode("A_WS");
        area.setName("Area WorkShift");
        area.setStatus(EntityStatus.ACTIVE);
        area = areaRepository.save(area);

        // 2. Tạo Store
        Store store = new Store();
        store.setCode("ST_WS");
        store.setName("Store WorkShift");
        store.setArea(area);
        store.setStatus(EntityStatus.ACTIVE);
        savedStore = storeRepository.save(store);

        // 3. Tạo Employee
        Employee emp = new Employee();
        emp.setEmail("emp.ws@test.com");
        emp.setFullName("Nhân viên Ca");
        emp.setPasswordHash("hash");
        emp.setStore(savedStore);
        emp.setStatus(EntityStatus.ACTIVE);
        savedEmployee = employeeRepository.save(emp);
    }

    @Test
    void checkIn_Success_ShouldCreateWorkShift() {
        WorkShiftRequest request = new WorkShiftRequest();
        request.setStoreId(savedStore.getId());
        request.setEmployeeId(savedEmployee.getId());
        request.setShiftDate(LocalDate.now());
        request.setStartTime(LocalTime.of(8, 0));
        request.setNotes("Đi làm đúng giờ");

        WorkShiftResponse response = workShiftService.checkIn(request);

        assertNotNull(response.getId());
        assertEquals("Nhân viên Ca", response.getEmployeeName());
        assertEquals("Store WorkShift", response.getStoreName());
        assertNull(response.getEndTime(), "Lúc check-in thì chưa có giờ check-out");

        WorkShift dbShift = workShiftRepository.findById(response.getId()).orElseThrow();
        assertEquals("Đi làm đúng giờ", dbShift.getNotes());
    }

    @Test
    void checkOut_Success_ShouldSetEndTime() {
        // Tạo ca làm việc đang dở dang (chưa có endTime)
        WorkShift shift = new WorkShift();
        shift.setStore(savedStore);
        shift.setEmployee(savedEmployee);
        shift.setShiftDate(LocalDate.now());
        shift.setStartTime(LocalTime.of(8, 0));
        shift = workShiftRepository.save(shift);

        // Gọi hàm checkOut
        WorkShiftResponse response = workShiftService.checkOut(shift.getId());

        assertNotNull(response.getEndTime(), "Hàm checkout phải lưu lại giờ kết thúc");

        WorkShift dbShift = workShiftRepository.findById(response.getId()).orElseThrow();
        assertNotNull(dbShift.getEndTime());
    }

    @Test
    void getAllShifts_ShouldReturnListWithMappedNames() {
        WorkShift shift = new WorkShift();
        shift.setStore(savedStore);
        shift.setEmployee(savedEmployee);
        shift.setShiftDate(LocalDate.now());
        shift.setStartTime(LocalTime.of(13, 0));
        workShiftRepository.save(shift);

        List<WorkShiftResponse> list = workShiftService.getAllShifts();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.getEmployeeName().equals("Nhân viên Ca")));
    }
}