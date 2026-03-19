package org.example.backend9.service.auth;

import org.example.backend9.dto.request.RegisterRequest;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // --- TEST PHẦN REGISTER ---

    @Test
    @DisplayName("Register: Thành công khi dữ liệu hợp lệ")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("123");
        request.setConfirmPassword("123");
        request.setFullName("Ngoc");

        when(passwordEncoder.encode("123")).thenReturn("encoded_123");
        when(employeeRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals(EntityStatus.INACTIVE, result.getStatus());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Register: Thất bại khi mật khẩu xác nhận không khớp")
    void register_Fail_PasswordNotMatch() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("123");
        request.setConfirmPassword("456"); // Khác nhau

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Mật khẩu xác nhận không khớp!", exception.getMessage());
    }

    @Test
    @DisplayName("Register: Thất bại khi email đã tồn tại")
    void register_Fail_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existed@gmail.com");
        request.setPassword("123");
        request.setConfirmPassword("123");

        when(employeeRepository.existsByEmail("existed@gmail.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email này đã được sử dụng trong hệ thống!", exception.getMessage());
    }

    // --- TEST PHẦN VERIFY EMAIL ---

    @Test
    @DisplayName("VerifyEmail: Thành công khi token đúng")
    void verifyEmail_Success() {
        String token = "valid-token";
        Employee emp = new Employee();
        emp.setStatus(EntityStatus.INACTIVE);
        emp.setVerificationToken(token);

        when(employeeRepository.findByVerificationToken(token)).thenReturn(Optional.of(emp));

        authService.verifyEmail(token);

        assertEquals(EntityStatus.ACTIVE, emp.getStatus());
        assertNull(emp.getVerificationToken());
        verify(employeeRepository).save(emp);
    }

    @Test
    @DisplayName("VerifyEmail: Thất bại khi token không tồn tại")
    void verifyEmail_Fail_InvalidToken() {
        String invalidToken = "wrong-token";
        when(employeeRepository.findByVerificationToken(invalidToken)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.verifyEmail(invalidToken));
        assertEquals("Mã xác thực không hợp lệ hoặc đã hết hạn.", exception.getMessage());
    }
}