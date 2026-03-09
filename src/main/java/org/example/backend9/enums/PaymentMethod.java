package org.example.backend9.enums;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    QR_CODE("Chuyển khoản QR"),
    CARD("Thẻ/POS");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}