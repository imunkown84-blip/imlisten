package com.immusic.service;

import com.immusic.dto.auth.AuthResponse;
import com.immusic.dto.auth.LoginRequest;
import com.immusic.dto.auth.RegisterRequest;
import com.immusic.entity.AppUser;
import com.immusic.exception.DuplicateResourceException;
import com.immusic.repository.AppUserRepository;
import com.immusic.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String identifier = request.getEmail();
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        if (appUserRepository.existsByEmail(identifier.toLowerCase())) {
            throw new DuplicateResourceException("Username or email already registered");
        }

        AppUser user = AppUser.builder()
                .email(identifier.toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        appUserRepository.save(user);

        UserDetails userDetails = User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

        return buildAuthResponse(userDetails);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmail();
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        String email = identifier.toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        UserDetails userDetails = User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

        return buildAuthResponse(userDetails);
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .username(userDetails.getUsername())
                .build();
    }
}
