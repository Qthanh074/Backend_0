package org.example.backend9.enums;

public enum NotificationType {
    INFO("Thông tin"),
    WARNING("Cảnh báo"),
    ERROR("Lỗi"),
    SUCCESS("Thành công");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}