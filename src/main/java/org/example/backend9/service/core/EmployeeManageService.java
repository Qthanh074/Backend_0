package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.EmployeeCreateRequest; // Nhớ tạo file DTO này
import org.example.backend9.dto.request.core.EmployeeUpdateRequest;
import org.example.backend9.dto.response.core.EmployeeResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // Để mã hóa mật khẩu
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeManageService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeManageService(EmployeeRepository employeeRepository,
                                 StoreRepository storeRepository,
                                 PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Map từ Entity sang Response DTO
    private EmployeeResponse mapToResponse(Employee emp) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setEmail(emp.getEmail());
        res.setFullName(emp.getFullName());
        res.setPhone(emp.getPhone());
        res.setRole(emp.getRole());
        res.setIsActive(emp.getStatus() == EntityStatus.ACTIVE);
        if (emp.getStore() != null) {
            res.setStoreId(emp.getStore().getId());
            res.setStoreName(emp.getStore().getName());
        }
        return res;
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        Employee emp = new Employee();
        emp.setEmail(request.getEmail());
        emp.setFullName(request.getFullName());
        emp.setPhone(request.getPhone());
        emp.setRole(request.getRole());

        // 🔑 Quan trọng: Mã hóa mật khẩu trước khi lưu
        emp.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        emp.setStatus(EntityStatus.ACTIVE);

        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
            emp.setStore(store);
        }

        return mapToResponse(employeeRepository.save(emp));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Integer id, EmployeeUpdateRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        emp.setFullName(request.getFullName());
        emp.setPhone(request.getPhone());
        emp.setRole(request.getRole());

        // 👉 CẬP NHẬT TRẠNG THÁI TẠI ĐÂY
        if (request.getIsActive() != null) {
            emp.setStatus(request.getIsActive() ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
        }

        // Cập nhật mật khẩu nếu có nhập mới
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            emp.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Xử lý Store (giữ nguyên code cũ của bạn)
        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại"));
            emp.setStore(store);
        } else {
            emp.setStore(null);
        }

        return mapToResponse(employeeRepository.save(emp));
    }
    @Transactional
    public void toggleEmployeeStatus(Integer id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (emp.getStatus() == EntityStatus.ACTIVE) {
            emp.setStatus(EntityStatus.INACTIVE);
        } else {
            emp.setStatus(EntityStatus.ACTIVE);
        }
        employeeRepository.save(emp);
    }

    @Transactional
    public void deleteEmployee(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy nhân viên để xóa");
        }
        employeeRepository.deleteById(id);
    }
}