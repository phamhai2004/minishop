package com.example.minishop.security;

import com.example.minishop.entity.User;
import com.example.minishop.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        User user;

        try {

            Long userId = Long.valueOf(username);

            user = userRepository
                    .findById(userId)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Không tìm thấy user"
                            )
                    );

        } catch (NumberFormatException e) {

            user = userRepository
                    .findByEmail(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Không tìm thấy user"
                            )
                    );
        }

        return new CustomUserDetails(user);
    }
}