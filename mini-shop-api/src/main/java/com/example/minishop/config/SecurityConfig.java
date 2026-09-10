package com.example.minishop.config;

import com.example.minishop.security.JwtAuthenticationFilter;
import com.example.minishop.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler
    )
    {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2AuthenticationSuccessHandler))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                        // Registration - public
                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/register/request-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/register/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/register/complete").permitAll()

                        // Public product
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                        // Categories
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")

                        // Product options
                        .requestMatchers(HttpMethod.GET, "/product-options/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/product-options/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/product-options/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/product-options/**").hasRole("ADMIN")

                        // Public flashsale
                        .requestMatchers(HttpMethod.GET, "/flash-sales/**").permitAll()

                        // Public review
                        .requestMatchers(HttpMethod.GET, "/reviews/product/**").permitAll()

                        // Public voucher
                        .requestMatchers(HttpMethod.GET, "/voucher-catalog", "/voucher-catalog/**").permitAll()

                        // Shop registration/status
                        .requestMatchers(HttpMethod.POST, "/shops").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/shops/me").hasAnyRole("CUSTOMER", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/shops/me").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/shops/me/resubmit").hasRole("CUSTOMER")

                        // Public shop
                        .requestMatchers(HttpMethod.GET, "/shops/**").permitAll()

                        // Seller shop management
                        .requestMatchers("/seller/shop/**").hasRole("SELLER")


                        // Seller product
                        .requestMatchers("/seller/products/**").hasRole("SELLER")
                        .requestMatchers("/seller/flash-sales/**").hasRole("SELLER")
                        .requestMatchers("/seller/vouchers/**").hasRole("SELLER")

                        // Seller order
                        .requestMatchers("/seller/orders/**").hasRole("SELLER")
                        .requestMatchers("/seller/flash-sales/**").hasRole("SELLER")
                        .requestMatchers("/seller/vouchers/**").hasRole("SELLER")

                        // Customer order
                        .requestMatchers(HttpMethod.POST, "/orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/orders/my").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/cancel").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/confirm-received").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/orders/*").hasRole("CUSTOMER")

                        // Current user
                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/me/avatar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/email/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/email/resend").permitAll()

                        // Recommendation
                        .requestMatchers(HttpMethod.GET, "/users/me/recommendations").hasRole("CUSTOMER")

                        // Shop Chat
                        .requestMatchers(HttpMethod.POST, "/shop-chat/conversations/shops/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/shop-chat/conversations/shops/*/products/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/shop-chat/conversations/*/product-context/close").hasRole("CUSTOMER")
                        .requestMatchers("/shop-chat/**").hasAnyRole("CUSTOMER", "SELLER")

                        // AI Chat
                        .requestMatchers(HttpMethod.POST, "/chat").hasRole("CUSTOMER")

                        // Admin
                        .requestMatchers("/admin/orders/**").hasRole("ADMIN")
                        .requestMatchers("/admin/shops/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Payment  
                        .requestMatchers(HttpMethod.PATCH, "/payments/orders/*/paid").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/vnpay/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/payments/vnpay/return", "/api/payments/vnpay/ipn").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/**").hasRole("CUSTOMER")

                        // Cart, wishlist, address
                        .requestMatchers("/cart/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/wishlist/count/**").permitAll()
                        .requestMatchers("/wishlist/**").hasRole("CUSTOMER")
                        .requestMatchers("/addresses/**").hasRole("CUSTOMER")
                        .requestMatchers("/my-vouchers/**").hasRole("CUSTOMER")

                        // Review
                        .requestMatchers(HttpMethod.GET, "/reviews/my", "/reviews/my/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/reviews").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").hasRole("CUSTOMER")

                        // Voucher management
                        .requestMatchers("/vouchers/**").hasRole("ADMIN")

                        // Stock
                        .requestMatchers("/stock-movements/**").hasAnyRole("ADMIN", "SELLER")

                        // Notifications
                        .requestMatchers("/notifications/**").hasAnyRole("CUSTOMER", "SELLER")

                        // Dashboard
                        .requestMatchers("/seller/dashboard/**").hasRole("SELLER")
                        .requestMatchers("/admin/dashboard/**").hasRole("ADMIN")

                        // Shop follow
                        .requestMatchers("/shop-follows/**").hasRole("CUSTOMER")

                        // WebSocket handshake
                        .requestMatchers("/ws", "/ws/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${frontend.url}") String frontendUrl
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        frontendUrl
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }
}