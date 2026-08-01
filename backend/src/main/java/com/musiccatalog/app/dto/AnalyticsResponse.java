package com.musiccatalog.app.dto;

import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
        long totalTracks,
        double averageRating,
        double averageDurationSeconds,
        Map<String, Long> tracksByGenre,
        Map<String, Long> tracksByArtist,
        Map<Integer, Long> tracksByReleaseYear,
        Map<String, Long> ratingDistribution,
        List<DurationBucket> durationHistogram
) {
    public record DurationBucket(String bucketLabel, long count) {}
}
