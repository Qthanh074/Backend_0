package org.example.backend9.dto.response.core;

import lombok.Data;

@Data
public class RolePermissionResponse {
    private String role;
    private String globalSettings;
    private String dashboardSettings;
    private String menuPermissions;
}