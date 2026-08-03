package com.immusic.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    private String basedOn;
    private List<RecommendedTrack> recommendations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedTrack {
        private Long appleCatalogId;
        private String title;
        private String artistName;
        private String genre;
        private String artworkUrl;
        private String previewUrl;
        private String reason;
        private double score;
    }
}
