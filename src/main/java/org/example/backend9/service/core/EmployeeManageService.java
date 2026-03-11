package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.EmployeeUpdateRequest;
import org.example.backend9.dto.response.core.EmployeeResponse;
import org.example.backend9.entity.core.Employee;
import org.example.backend9.entity.core.Store;
import org.example.backend9.enums.EntityStatus;
import org.example.backend9.repository.core.EmployeeRepository;
import org.example.backend9.repository.core.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeManageService {
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;

    public EmployeeManageService(EmployeeRepository employeeRepository, StoreRepository storeRepository) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setCode(emp.getCode());
        res.setFullName(emp.getFullName());
        res.setEmail(emp.getEmail());
        res.setPhone(emp.getPhone());
        res.setRole(emp.getRole());
        res.setStatus(emp.getStatus());
        if (emp.getStore() != null) {
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
    public EmployeeResponse updateEmployee(Integer id, EmployeeUpdateRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        emp.setFullName(request.getFullName());
        emp.setPhone(request.getPhone());
        emp.setRole(request.getRole());

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
        emp.setStatus(emp.getStatus() == EntityStatus.ACTIVE ? EntityStatus.LOCKED : EntityStatus.ACTIVE);
        employeeRepository.save(emp);
    }
}