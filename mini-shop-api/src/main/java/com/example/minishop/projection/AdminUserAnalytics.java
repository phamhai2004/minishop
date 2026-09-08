package com.example.minishop.projection;

import com.example.minishop.constant.AuthProvider;
import com.example.minishop.constant.Role;

import java.time.LocalDateTime;

public interface AdminUserAnalytics {

    Long getUserId();

    String getFullName();

    String getEmail();

    String getPhone();

    Role getRole();

    Boolean getActive();

    Boolean getEmailVerified();

    AuthProvider getAuthProvider();

    LocalDateTime getCreatedAt();
}