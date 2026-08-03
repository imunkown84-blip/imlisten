package com.immusic.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String email;
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
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

    @NotBlank(message = "Username or email is required")
    public String getIdentifier() {
        return getEmail();
    }
}
