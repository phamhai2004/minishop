package com.example.minishop.mapper;

import com.example.minishop.dto.request.RegisterRequest;
import com.example.minishop.dto.response.UserResponse;
import com.example.minishop.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());


        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setRoleName(user.getRole().getDisplayName());
        response.setActive(user.getActive());
        response.setAvatarUrl(user.getAvatarUrl());

        return response;
    }
}