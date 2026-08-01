package com.musiccatalog.app.dto;

import java.time.Instant;
import java.time.LocalDate;

public record LibraryItemResponse(
        Long id,
        Long appleCatalogId,
        String title,
        String artistName,
        String genre,
        LocalDate releaseDate,
        Long durationMillis,
        String artworkUrl,
        Integer userRating,
        String userNotes,
        Instant createdAt,
        Instant updatedAt
) {}
