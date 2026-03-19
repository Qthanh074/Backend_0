package org.example.backend9.service.core;

import jakarta.persistence.EntityManager;
import org.example.backend9.dto.request.core.RolePermissionRequest;
import org.example.backend9.dto.response.core.RolePermissionResponse;
import org.example.backend9.entity.core.RolePermission;
import org.example.backend9.repository.core.RolePermissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Tự động dọn dẹp rác trong Database thật sau khi test
class RolePermissionServiceIntegrationTest {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private EntityManager entityManager;

    // Chặn Google Sheet API để test không bị lỗi
    @MockBean
    private org.example.backend9.service.GoogleSheetService googleSheetService;

    // ================= TEST HÀM LẤY DANH SÁCH =================
    @Test
    void getAllRoles_ShouldReturnAllRolesInDatabase() {
        // 1. Chuẩn bị: Bơm 2 role nháp vào Database
        RolePermission role1 = new RolePermission();
        role1.setRole("SUPER_ADMIN");
        rolePermissionRepository.save(role1);

        RolePermission role2 = new RolePermission();
        role2.setRole("CASHIER");
        rolePermissionRepository.save(role2);

        // Ép dữ liệu xuống DB và xóa đệm để đảm bảo hàm getAll đọc từ DB thật
        entityManager.flush();
        entityManager.clear();

        // 2. Thực thi
        List<RolePermissionResponse> result = rolePermissionService.getAllRoles();

        // 3. Kiểm tra
        assertFalse(result.isEmpty(), "Danh sách Role không được rỗng");

        // Đảm bảo lấy ra được đúng các role vừa bơm vào
        boolean containsSuperAdmin = result.stream().anyMatch(r -> r.getRole().equals("SUPER_ADMIN"));
        boolean containsCashier = result.stream().anyMatch(r -> r.getRole().equals("CASHIER"));

        assertTrue(containsSuperAdmin, "Phải chứa role SUPER_ADMIN");
        assertTrue(containsCashier, "Phải chứa role CASHIER");
    }

    // ================= TEST HÀM SAVE OR UPDATE (KỊCH BẢN THÊM MỚI) =================
    @Test
    void saveOrUpdateRole_WhenRoleIsNew_ShouldCreateNewRole_AndConvertToUpperCase() {
        // 1. Chuẩn bị request với tên role viết thường
        RolePermissionRequest request = new RolePermissionRequest();
        request.setRole("inventory_manager"); // Cố tình viết thường để test logic toUpperCase()

        // 2. Thực thi
        RolePermissionResponse response = rolePermissionService.saveOrUpdateRole(request);

        entityManager.flush();
        entityManager.clear();

        // 3. Kiểm tra kết quả trả về
        assertNotNull(response);
        assertEquals("INVENTORY_MANAGER", response.getRole(), "Tên role phải được tự động viết hoa");

        // 4. Lôi thẳng từ Database ra kiểm chứng
        Optional<RolePermission> savedRole = rolePermissionRepository.findById("INVENTORY_MANAGER");
        assertTrue(savedRole.isPresent(), "Role mới phải được lưu vào Database thật");
        assertEquals("INVENTORY_MANAGER", savedRole.get().getRole());
    }

    // ================= TEST HÀM SAVE OR UPDATE (KỊCH BẢN CẬP NHẬT) =================
    @Test
    void saveOrUpdateRole_WhenRoleExists_ShouldUpdateExistingRole() {
        // 1. Bơm 1 role gốc vào DB trước
        RolePermission existingRole = new RolePermission();
        existingRole.setRole("STAFF");
        rolePermissionRepository.save(existingRole);

        // Ép lưu thẳng xuống DB để tạo môi trường giống thật 100%
        entityManager.flush();
        entityManager.clear();

        // 2. Tạo request cập nhật lại chính role đó
        RolePermissionRequest request = new RolePermissionRequest();
        request.setRole("STAFF"); // Viết thường để xem nó có map đúng vào "STAFF" đang có không

        // 3. Thực thi
        RolePermissionResponse response = rolePermissionService.saveOrUpdateRole(request);

        entityManager.flush();
        entityManager.clear();

        // 4. Kiểm tra
        assertNotNull(response);
        assertEquals("STAFF", response.getRole());

        // Đảm bảo nó không tạo ra 2 bản ghi (một cái 'STAFF', một cái 'staff')
        long count = rolePermissionRepository.findAll().stream()
                .filter(r -> r.getRole().equalsIgnoreCase("STAFF"))
                .count();
        assertEquals(1, count, "Chỉ được phép tồn tại 1 bản ghi duy nhất cho role này");
    }
}