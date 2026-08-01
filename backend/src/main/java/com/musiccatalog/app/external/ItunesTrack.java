package com.musiccatalog.app.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItunesTrack(
        Long trackId,
        String trackName,
        String artistName,
        String collectionName,
        String primaryGenreName,
        String releaseDate,
        Long trackTimeMillis,
        String artworkUrl100,
        Double trackPrice,
        String previewUrl
) {}
