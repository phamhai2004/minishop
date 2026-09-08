package com.example.minishop.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor
        implements ChannelInterceptor {

    private final JwtService jwtService;

    private final CustomUserDetailsService
            userDetailsService;

    public WebSocketAuthChannelInterceptor(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService =
                userDetailsService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        message
                );

        if (
                StompCommand.CONNECT
                        .equals(
                                accessor.getCommand()
                        )
        ) {

            String authorization =
                    accessor.getFirstNativeHeader(
                            "Authorization"
                    );

            if (
                    authorization == null
                            ||
                            !authorization.startsWith(
                                    "Bearer "
                            )
            ) {
                throw new IllegalArgumentException(
                        "Thiếu WebSocket Authorization token"
                );
            }

            String token =
                    authorization.substring(7);

            Long userId =
                    jwtService.extractUserId(
                            token
                    );

            if (
                    !jwtService.isValidToken(
                            token,
                            userId
                    )
            ) {
                throw new IllegalArgumentException(
                        "WebSocket token không hợp lệ"
                );
            }

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(
                                    String.valueOf(
                                            userId
                                    )
                            );

            if (!userDetails.isEnabled()) {
                throw new IllegalArgumentException(
                        "Tài khoản đã bị khóa"
                );
            }

            UsernamePasswordAuthenticationToken
                    authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails
                                    .getAuthorities()
                    );

            accessor.setUser(
                    authentication
            );
        }

        return message;
    }
}