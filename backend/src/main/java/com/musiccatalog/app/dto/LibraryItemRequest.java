package com.musiccatalog.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Payload for creating (POST) or updating (PUT) a library entry.
 * On create, catalog fields (title/artist/etc.) are typically populated from
 * the iTunes search result the user picked. On update, only userRating and
 * userNotes are generally mutated, but the whole record is accepted for simplicity.
 */
public record LibraryItemRequest(
        @NotNull(message = "appleCatalogId is required")
        Long appleCatalogId,

        @NotBlank(message = "title is required")
        String title,

        @NotBlank(message = "artistName is required")
        String artistName,

        String genre,

        LocalDate releaseDate,

        Long durationMillis,

        String artworkUrl,

        @Min(value = 1, message = "userRating must be between 1 and 5")
        @Max(value = 5, message = "userRating must be between 1 and 5")
        Integer userRating,

        String userNotes
) {}
