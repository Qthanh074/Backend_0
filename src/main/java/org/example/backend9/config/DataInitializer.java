package org.example.backend9.config;

import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.RolePermission;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.enums.UserRole;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.RolePermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository,
                           RolePermissionRepository rolePermissionRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // 1. Khởi tạo các Roles mặc định từ Enum UserRole vào bảng role_permissions
        for (UserRole userRole : UserRole.values()) {
            if (!rolePermissionRepository.existsById(userRole.name())) {
                RolePermission role = new RolePermission();
                role.setRole(userRole.name());

                // Khởi tạo các chuỗi JSON rỗng mặc định để tránh lỗi NullPointerException
                role.setGlobalSettings("{}");
                role.setDashboardSettings("{}");
                role.setMenuPermissions("{}");

                rolePermissionRepository.save(role);
            }
        }

        // 2. Khởi tạo các tài khoản demo
        createDemoEmployee("super@example.com", UserRole.SUPER_ADMIN, "Super Admin", "EMP001", "0900000001");
        createDemoEmployee("admin@example.com", UserRole.ADMIN, "System Admin", "EMP002", "0900000002");
    }

    private void createDemoEmployee(String email, UserRole roleEnum, String fullName, String code, String phone) {
        if (employeeRepository.findByEmail(email).isEmpty()) {
            Employee employee = new Employee();
            employee.setEmail(email);

            // Mã hóa mật khẩu giống hệ thống cũ
            employee.setPasswordHash(passwordEncoder.encode("Default123!"));

            employee.setFullName(fullName);
            employee.setCode(code);
            employee.setPhone(phone);

            // Set trạng thái ACTIVE để có thể đăng nhập ngay
            employee.setStatus(EntityStatus.ACTIVE);
            employee.setVerificationToken(null);

            // Gán Role dưới dạng chuỗi (Phù hợp với thiết kế của Employee Entity mới)
            employee.setRole(roleEnum.name());

            employeeRepository.save(employee);
            System.out.println(" -> Đã tạo tài khoản demo: " + email + " | Phân quyền: " + roleEnum.name());
        }
    }
}