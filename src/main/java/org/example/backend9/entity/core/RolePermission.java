package org.example.backend9.entity.core;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Data @NoArgsConstructor @AllArgsConstructor
public class RolePermission {

    @Id
    private String role; // ADMIN, MANAGER, ACCOUNTANT, CASHIER

    @Column(columnDefinition = "JSON")
    private String globalSettings;

    @Column(columnDefinition = "JSON")
    private String dashboardSettings;

    @Column(columnDefinition = "JSON")
    private String menuPermissions;
}
