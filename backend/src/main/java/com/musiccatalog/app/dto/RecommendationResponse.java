package com.musiccatalog.app.dto;

import java.util.List;

public record RecommendationResponse(
        String basedOn,
        List<RecommendedTrack> recommendations
) {
    public record RecommendedTrack(
            Long appleCatalogId,
            String title,
            String artistName,
            String genre,
            String artworkUrl,
            String previewUrl,
            String reason,
            double score
    ) {}
}
