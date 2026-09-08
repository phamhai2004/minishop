package com.example.minishop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @NotBlank(message = "Họ tên không được bỏ trống")
    @Size(
            max = 100,
            message = "Họ tên không được vượt quá 100 ký tự"
    )
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(
            regexp = "^$|^[0-9]{9,11}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}