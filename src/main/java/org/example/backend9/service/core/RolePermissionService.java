package org.example.backend9.service.core;

import org.example.backend9.dto.request.core.RolePermissionRequest;
import org.example.backend9.dto.response.core.RolePermissionResponse;
import org.example.backend9.entity.core.RolePermission;
import org.example.backend9.repository.core.RolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    private RolePermissionResponse mapToResponse(RolePermission rp) {
        RolePermissionResponse res = new RolePermissionResponse();
        res.setRole(rp.getRole());
        res.setGlobalSettings(rp.getGlobalSettings());
        res.setDashboardSettings(rp.getDashboardSettings());
        res.setMenuPermissions(rp.getMenuPermissions());
        return res;
    }

    public List<RolePermissionResponse> getAllRoles() {
        return rolePermissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RolePermissionResponse saveOrUpdateRole(RolePermissionRequest request) {
        RolePermission rp = rolePermissionRepository.findById(request.getRole())
                .orElse(new RolePermission());

        rp.setRole(request.getRole().toUpperCase());
        rp.setGlobalSettings(request.getGlobalSettings());
        rp.setDashboardSettings(request.getDashboardSettings());
        rp.setMenuPermissions(request.getMenuPermissions());

        return mapToResponse(rolePermissionRepository.save(rp));
    }
}