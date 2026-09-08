package com.example.minishop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateShopRequest {

    @NotBlank(message = "Tên shop không được bỏ trống")
    @Size(
            min = 3,
            max = 150,
            message = "Tên shop phải từ 3 đến 150 ký tự"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Mô tả shop không được vượt quá 1000 ký tự"
    )
    private String description;

    @NotBlank(message = "Số điện thoại shop không được bỏ trống")
    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Địa chỉ lấy hàng không được bỏ trống")
    private String pickupAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
}