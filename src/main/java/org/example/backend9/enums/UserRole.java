package org.example.backend9.enums;

public enum UserRole {
    CUSTOMER("Khách hàng"),
    STAFF("Nhân viên bán hàng"),
    MANAGER("Quản lý cửa hàng"),
    ADMIN("Quản trị viên hệ thống"),
    SUPER_ADMIN("Siêu quản trị viên");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
