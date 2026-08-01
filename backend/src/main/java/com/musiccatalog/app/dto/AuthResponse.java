package com.musiccatalog.app.dto;

public record AuthResponse(
        String token,
        String username
) {}
