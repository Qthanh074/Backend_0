// File: src/main/java/org/example/backend9/controller/auth/AuthController.java
package org.example.backend9.controller.auth;

import org.example.backend9.dto.response.ApiResponse;
import org.example.backend9.dto.request.LoginRequest;
import org.example.backend9.dto.response.LoginResponse;
import org.example.backend9.dto.request.RegisterRequest;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.security.JwtTokenProvider;
import org.example.backend9.service.auth.AuthService;
import org.example.backend9.service.auth.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final TokenBlacklistService blacklistService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtTokenProvider tokenProvider, AuthService authService,
                          TokenBlacklistService blacklistService, EmployeeRepository employeeRepository,
                          PasswordEncoder passwordEncoder) {
        this.tokenProvider = tokenProvider;
        this.authService = authService;
        this.blacklistService = blacklistService;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Employee employee = employeeRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        // Kiểm tra mật khẩu và trạng thái ACTIVE
        if (employee == null || employee.getStatus() != EntityStatus.ACTIVE ||
                !passwordEncoder.matches(loginRequest.getPassword(), employee.getPasswordHash())) {
            return ResponseEntity.status(401).body(new ApiResponse<>(
                    false, "Email/mật khẩu không đúng hoặc tài khoản chưa kích hoạt", null));
        }

        String jwt = tokenProvider.generateTokenFromEmail(employee.getEmail());
        LoginResponse response = new LoginResponse(jwt, "Bearer", employee.getRole(), employee.getFullName());

        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký thành công. Vui lòng kiểm tra email!", null));
    }

    @GetMapping("/verify")
    public void verifyAccount(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        authService.verifyEmail(token);
        // Có thể cấu hình URL này trong application.properties
        response.sendRedirect("http://localhost:5173/login?verified=true");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            long expiryDuration = tokenProvider.getExpiryDuration(jwt);
            blacklistService.blacklistToken(jwt, expiryDuration);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng xuất thành công", null));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Không tìm thấy token", null));
    }
}