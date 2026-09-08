package com.example.minishop.projection;

import com.example.minishop.constant.AuthProvider;

public interface AuthProviderStat {

    AuthProvider getAuthProvider();

    Long getUsers();
}