package org.example.backend9.dto.request.sales;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank(message = "Mã khách hàng không được để trống")
    private String code;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String email;
    private String address;
    private Integer areaId; // Truyền lên ID khu vực (nếu có)
    private Boolean canPlaceOrder;
}