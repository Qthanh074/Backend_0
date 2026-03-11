package org.example.backend9.service.auth;

import org.example.backend9.dto.request.RegisterRequest;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.enums.UserRole;
import org.example.backend9.repository.core.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Employee register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống!");
        }

        Employee emp = new Employee();
        emp.setEmail(request.getEmail());
        emp.setFullName(request.getFullName());
        emp.setPhone(request.getPhone());
        emp.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        emp.setStatus(EntityStatus.INACTIVE);
        emp.setVerificationToken(UUID.randomUUID().toString());

        // --- SỬA TẠI ĐÂY: Gán role mặc định theo Enum ---
        emp.setRole(UserRole.STAFF.name());

        emp.setCode("EMP" + System.currentTimeMillis() % 10000);

        Employee saved = employeeRepository.save(emp);

        // TODO: Gọi EmailService gửi mail
        System.out.println("Gửi mail xác thực đến: " + emp.getEmail() + " với token: " + emp.getVerificationToken());

        return saved;
    }

    @Transactional
    public void verifyEmail(String token) {
        Employee emp = employeeRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn."));

        emp.setStatus(EntityStatus.ACTIVE);
        emp.setVerificationToken(null);
        employeeRepository.save(emp);
    }
}