package com.example.minishop.security;

import com.example.minishop.constant.AuthProvider;
import com.example.minishop.constant.Role;
import com.example.minishop.entity.RefreshToken;
import com.example.minishop.entity.User;
import com.example.minishop.repository.RefreshTokenRepository;
import com.example.minishop.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String registrationId =
                ((OAuth2AuthenticationToken) authentication)
                        .getAuthorizedClientRegistrationId();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        String providerId;

        if ("google".equals(registrationId)) {

            providerId = oauth2User.getAttribute("sub");

        } else if ("facebook".equals(registrationId)) {

            providerId = oauth2User.getAttribute("id");

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "OAuth2 provider không được hỗ trợ: "
                            + registrationId
            );

            return;
        }

        AuthProvider authProvider;

        if ("google".equals(registrationId)) {
            authProvider = AuthProvider.GOOGLE;
        } else {
            authProvider = AuthProvider.FACEBOOK;
        }

        User user = userRepository
                .findByAuthProviderAndProviderId(
                        authProvider,
                        providerId
                )
                .orElse(null);

        if (user == null
                && email != null
                && !email.isBlank()) {

            user = userRepository
                    .findByEmail(email)
                    .orElse(null);
        }

        if (user == null) {

            user = createOAuth2User(
                    email,
                    name,
                    providerId,
                    authProvider
            );

        } else {

            if (user.getEmail() == null
                    && email != null
                    && !email.isBlank()) {

                user.setEmail(email);
            }

            if (email != null && !email.isBlank()) {
                user.setEmailVerified(true);
            }

            if ((user.getFullName() == null
                    || user.getFullName().isBlank())
                    && name != null
                    && !name.isBlank()) {

                user.setFullName(name);
            }

            userRepository.save(user);
        }


        userRepository.save(user);

        if (user.getEmail() == null || user.getEmail().isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "OAuth2 user không có email"
            );

            return;
        }

        String accessToken =
                jwtService.generateToken(user.getId());

        String refreshToken =
                jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity =
                new RefreshToken();

        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiryDate(
                LocalDateTime.now().plusDays(30)
        );
        refreshTokenEntity.setRevoked(false);

        refreshTokenRepository.save(refreshTokenEntity);

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                frontendUrl + "/oauth2/callback"
                        )
                        .queryParam(
                                "accessToken",
                                accessToken
                        )
                        .queryParam(
                                "refreshToken",
                                refreshToken
                        )
                        .queryParam(
                                "userId",
                                user.getId()
                        )
                        .queryParam(
                                "fullName",
                                user.getFullName()
                        )
                        .queryParam(
                                "email",
                                user.getEmail()
                        )
                        .queryParam(
                                "role",
                                user.getRole()
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private User createOAuth2User(
            String email,
            String name,
            String providerId,
            AuthProvider authProvider
    ) {

        User user = new User();

        user.setEmail(
                email != null && !email.isBlank()
                        ? email
                        : null
        );

        user.setFullName(
                name != null && !name.isBlank()
                        ? name
                        : "OAuth User"
        );

        user.setPassword(null);

        user.setRole(Role.CUSTOMER);

        user.setActive(true);

        user.setEmailVerified(
                email != null && !email.isBlank()
        );

        user.setAuthProvider(authProvider);

        user.setProviderId(providerId);

        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}