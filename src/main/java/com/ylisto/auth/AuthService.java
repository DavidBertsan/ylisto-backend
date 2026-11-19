package com.ylisto.auth;

import com.ylisto.security.TokenService;
import com.ylisto.user.User;

import com.ylisto.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.username())                        // 1. find
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword())) // 2. verify
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        return new LoginResponse(tokenService.issue(user));
    }
}
