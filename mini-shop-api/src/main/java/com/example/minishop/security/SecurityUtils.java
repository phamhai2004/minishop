package com.example.minishop.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (CustomUserDetails)
                authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {

        return getCurrentUser()
                .getUser()
                .getId();

    }

    public static String getCurrentEmail() {

        return getCurrentUser()
                .getUsername();

    }
}