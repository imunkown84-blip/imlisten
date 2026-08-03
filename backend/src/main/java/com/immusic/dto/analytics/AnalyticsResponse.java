package com.immusic.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

    private int totalTracks;
    private double averageRating;
    private double averageDurationSeconds;
    private Map<String, Integer> tracksByGenre;
    private Map<String, Integer> tracksByArtist;
    private Map<String, Integer> tracksByReleaseYear;
    private Map<String, Integer> ratingDistribution;
    private List<DurationBucket> durationHistogram;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DurationBucket {
        private String bucketLabel;
        private int count;
    }
}
