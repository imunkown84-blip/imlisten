package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.AuthResponse;
import com.musiccatalog.app.dto.LoginRequest;
import com.musiccatalog.app.dto.RegisterRequest;
import com.musiccatalog.app.exception.DuplicateResourceException;
import com.musiccatalog.app.model.User;
import com.musiccatalog.app.repository.UserRepository;
import com.musiccatalog.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(request.username());
        return new AuthResponse(token, request.username());
    }
}
