package com.musiccatalog.app.dto;

/**
 * Normalized shape of an iTunes search hit for a song, exposed to the frontend.
 * Field names deliberately mirror LibraryItemRequest so a search result can be
 * saved to the library with no client-side remapping.
 */
public record SearchResultDto(
        Long appleCatalogId,
        String title,
        String artistName,
        String collectionName,
        String genre,
        String releaseDate,
        Long durationMillis,
        String artworkUrl,
        Double trackPrice,
        String previewUrl
) {}
