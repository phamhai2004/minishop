package com.example.minishop.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyRegistrationRequest {

    @NotBlank(message = "Token không được bỏ trống")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}