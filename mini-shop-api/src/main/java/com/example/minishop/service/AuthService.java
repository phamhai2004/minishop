package com.example.minishop.service;

import com.example.minishop.dto.request.LoginRequest;
import com.example.minishop.dto.request.LogoutRequest;
import com.example.minishop.dto.request.RefreshTokenRequest;
import com.example.minishop.dto.response.LoginResponse;
import com.example.minishop.entity.RefreshToken;
import com.example.minishop.entity.User;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.repository.RefreshTokenRepository;
import com.example.minishop.repository.UserRepository;
import com.example.minishop.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        // Kiểm tra email đã được xác minh chưa
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Vui lòng xác minh email trước khi đăng nhập"
            );
        }

        String accessToken =
                jwtService.generateToken(user.getId());

        String refreshToken =
                jwtService.generateRefreshToken(user.getId());

        RefreshToken entity = new RefreshToken();
        entity.setToken(refreshToken);
        entity.setUser(user);
        entity.setExpiryDate(LocalDateTime.now().plusDays(30));
        entity.setRevoked(false);

        refreshTokenRepository.save(entity);

        LoginResponse response = new LoginResponse();

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");

        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại"));

        refreshToken.setRevoked(true);
    }

    @Transactional
    public LoginResponse refresh(
            RefreshTokenRequest request
    ) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Refresh token không tồn tại"
                        )
                );

        if (refreshToken.getRevoked()) {
            throw new BadRequestException(
                    "Refresh token đã bị thu hồi"
            );
        }

        if (refreshToken.getExpiryDate() == null
                || refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Refresh token đã hết hạn"
            );
        }

        User user = refreshToken.getUser();

        if (user == null) {
            throw new BadRequestException(
                    "Refresh token không hợp lệ"
            );
        }

        String newAccessToken =
                jwtService.generateToken(
                        user.getId()
                );

        return new LoginResponse(
                newAccessToken,
                refreshToken.getToken()
        );
    }
}