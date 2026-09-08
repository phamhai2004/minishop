package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateShopRegistrationRequest {

    @NotBlank(message = "Tên shop không được bỏ trống")
    @Size(
            max = 150,
            message = "Tên shop không được vượt quá 150 ký tự"
    )
    private String name;

    @NotBlank(message = "Mô tả shop không được bỏ trống")
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

    @NotBlank(message = "Địa chỉ shop không được bỏ trống")
    @Size(
            max = 255,
            message = "Địa chỉ shop không được vượt quá 255 ký tự"
    )
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

    public String getAddress() {
        return pickupAddress;
    }

    public void setAddress(String address) {
        this.pickupAddress = address;
    }
}