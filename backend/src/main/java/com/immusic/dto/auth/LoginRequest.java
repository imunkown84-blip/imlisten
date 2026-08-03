package com.immusic.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    private String email;
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }

    public String getUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email;
    }

    public String getPassword() {
        return password;
    }
}
