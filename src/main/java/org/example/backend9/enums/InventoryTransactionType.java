package org.example.backend9.enums;

public enum InventoryTransactionType {
    IMPORT("Nhập kho"),
    EXPORT("Xuất kho"),
    TRANSFER("Điều chuyển kho"),
    ADJUSTMENT("Điều chỉnh");

    private final String description;

    InventoryTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}