package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.EmployeeCreateRequest;
import org.example.backend9.dto.request.core.EmployeeUpdateRequest;
import org.example.backend9.dto.response.core.EmployeeResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Đảm bảo dọn sạch DB thật sau khi test xong
class EmployeeManageServiceIntegrationTest {

    @Autowired
    private EmployeeManageService employeeManageService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Chặn Google Sheet API để test không bị lỗi khởi tạo
    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    private Store savedStore;

    @BeforeEach
    void setUp() {
        // Chuẩn bị 1 cửa hàng nháp trong DB để test chức năng gắn nhân viên vào cửa hàng
        Store store = new Store();
        store.setCode("ST_TEST_01");
        store.setName("Cửa hàng Test");
        store.setStatus(EntityStatus.ACTIVE);
        savedStore = storeRepository.save(store);
    }

    // ================= TEST CREATE (THÊM MỚI) =================
    @Test
    void createEmployee_Success_ShouldSaveAndEncodePassword() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmail("test.create@gmail.com");
        request.setFullName("Nguyễn Văn Tạo");
        request.setPhone("0988888888");
        request.setRole("STAFF");
        request.setPassword("matkhau123");
        request.setStoreId(savedStore.getId()); // Gắn vào cửa hàng vừa tạo

        // Thực thi
        EmployeeResponse response = employeeManageService.createEmployee(request);

        // Kiểm tra Response trả về
        assertNotNull(response.getId());
        assertEquals("test.create@gmail.com", response.getEmail());
        assertEquals(savedStore.getId(), response.getStoreId());
        assertTrue(response.getIsActive());

        // Kiểm tra trực tiếp trong DB xem Pass đã được băm (hash) chưa
        Employee savedEmp = employeeRepository.findById(response.getId()).orElseThrow();
        assertNotEquals("matkhau123", savedEmp.getPasswordHash(), "Mật khẩu lưu vào DB KHÔNG ĐƯỢC để nguyên gốc");
        assertTrue(passwordEncoder.matches("matkhau123", savedEmp.getPasswordHash()), "Mật khẩu băm phải khớp với mật khẩu gốc");
    }

    @Test
    void createEmployee_WhenEmailAlreadyExists_ShouldThrowException() {
        // Bơm 1 nhân viên vào DB trước
        Employee emp = new Employee();
        emp.setEmail("trung.email@gmail.com");
        emp.setFullName("Người cũ");
        emp.setPasswordHash("hash123");
        emp.setStatus(EntityStatus.ACTIVE);
        employeeRepository.save(emp);

        // Tạo request với email y hệt
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmail("trung.email@gmail.com"); // Cố tình để trùng

        // Kiểm tra lỗi văng ra
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeManageService.createEmployee(request);
        });
        assertEquals("Email này đã được sử dụng!", exception.getMessage());
    }

    // ================= TEST LẤY DANH SÁCH =================
    @Test
    void getAllEmployees_ShouldReturnMappedList() {
        Employee emp = new Employee();
        emp.setEmail("getall@gmail.com");
        emp.setFullName("Lấy Danh Sách");
        emp.setPasswordHash("hash");
        emp.setStatus(EntityStatus.ACTIVE);
        employeeRepository.save(emp);

        List<EmployeeResponse> list = employeeManageService.getAllEmployees();

        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(e -> e.getEmail().equals("getall@gmail.com")));
    }

    // ================= TEST UPDATE (CẬP NHẬT) =================
    @Test
    void updateEmployee_Success_ShouldUpdateDataAndStore() {
        // 1. Tạo nhân viên gốc
        Employee emp = new Employee();
        emp.setEmail("update.me@gmail.com");
        emp.setFullName("Tên cũ");
        emp.setPhone("0111");
        emp.setPasswordHash("oldHash");
        emp.setStatus(EntityStatus.ACTIVE);
        emp = employeeRepository.save(emp);

        // 2. Tạo request sửa thông tin
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setFullName("Tên đã sửa");
        request.setPhone("0999");
        request.setIsActive(false); // Chuyển thành Inactive
        request.setPassword("newPass123"); // Đổi pass
        request.setStoreId(savedStore.getId()); // Gắn vào store

        // 3. Thực thi update
        EmployeeResponse response = employeeManageService.updateEmployee(emp.getId(), request);

        // 4. Lôi từ DB ra kiểm chứng
        Employee updatedEmp = employeeRepository.findById(emp.getId()).orElseThrow();
        assertEquals("Tên đã sửa", updatedEmp.getFullName());
        assertEquals(EntityStatus.INACTIVE, updatedEmp.getStatus());
        assertNotNull(updatedEmp.getStore());
        assertEquals(savedStore.getId(), updatedEmp.getStore().getId());
        assertTrue(passwordEncoder.matches("newPass123", updatedEmp.getPasswordHash()), "Mật khẩu mới phải được băm và lưu lại");
    }

    // ================= TEST TOGGLE STATUS (BẬT/TẮT TRẠNG THÁI) =================
    @Test
    void toggleEmployeeStatus_ShouldSwitchStatus() {
        Employee emp = new Employee();
        emp.setEmail("toggle@gmail.com");
        emp.setFullName("Toggle Test");
        emp.setPasswordHash("hash");
        emp.setStatus(EntityStatus.ACTIVE); // Khởi tạo là ACTIVE
        emp = employeeRepository.save(emp);

        // Chạy lần 1: ACTIVE -> INACTIVE
        employeeManageService.toggleEmployeeStatus(emp.getId());
        Employee empAfterFirstToggle = employeeRepository.findById(emp.getId()).orElseThrow();
        assertEquals(EntityStatus.INACTIVE, empAfterFirstToggle.getStatus());

        // Chạy lần 2: INACTIVE -> ACTIVE
        employeeManageService.toggleEmployeeStatus(emp.getId());
        Employee empAfterSecondToggle = employeeRepository.findById(emp.getId()).orElseThrow();
        assertEquals(EntityStatus.ACTIVE, empAfterSecondToggle.getStatus());
    }

    // ================= TEST DELETE (XÓA CỨNG) =================
    @Test
    void deleteEmployee_WhenIdExists_ShouldDeleteHardFromDatabase() {
        Employee emp = new Employee();
        emp.setEmail("delete.me@gmail.com");
        emp.setFullName("Xóa cứng");
        emp.setPasswordHash("hash");
        emp.setStatus(EntityStatus.ACTIVE);
        emp = employeeRepository.save(emp);

        // Lệnh xóa
        employeeManageService.deleteEmployee(emp.getId());

        // Kiểm tra xem đã "bay màu" khỏi DB thật chưa
        Optional<Employee> deletedEmp = employeeRepository.findById(emp.getId());
        assertTrue(deletedEmp.isEmpty(), "Nhân viên phải bị xóa hoàn toàn khỏi DB");
    }

    @Test
    void deleteEmployee_WhenIdDoesNotExist_ShouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeManageService.deleteEmployee(99999);
        });
        assertEquals("Không tìm thấy nhân viên để xóa", exception.getMessage());
    }
}