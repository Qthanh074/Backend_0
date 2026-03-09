package org.example.backend9.enums;

public enum EntityStatus {
    ACTIVE,      // Đang hoạt động
    INACTIVE,    // Ngưng hoạt động (nhưng chưa khóa hẳn)
    LOCKED,      // Bị khóa (dành cho User/Employee vi phạm)
    PENDING      // Chờ duyệt
}
