package com.example.minishop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegistrationEmailRequest {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được bỏ trống")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}