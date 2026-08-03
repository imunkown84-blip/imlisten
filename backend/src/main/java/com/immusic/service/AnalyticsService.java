package com.immusic.service;

import com.immusic.dto.analytics.AnalyticsResponse;
import com.immusic.entity.LibraryItem;
import com.immusic.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LibraryItemRepository libraryItemRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(java.util.UUID userId) {
        List<LibraryItem> items = libraryItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (items.isEmpty()) {
            return AnalyticsResponse.builder()
                    .totalTracks(0)
                    .averageRating(0.0)
                    .averageDurationSeconds(0.0)
                    .tracksByGenre(new HashMap<>())
                    .tracksByArtist(new HashMap<>())
                    .tracksByReleaseYear(new HashMap<>())
                    .ratingDistribution(new HashMap<>())
                    .durationHistogram(new ArrayList<>())
                    .build();
        }

        int totalTracks = items.size();
        double sumRating = 0.0;
        int ratingCount = 0;
        double sumDurationMs = 0.0;
        int durationCount = 0;

        Map<String, Integer> genreMap = new HashMap<>();
        Map<String, Integer> artistMap = new HashMap<>();
        Map<String, Integer> yearMap = new HashMap<>();
        Map<String, Integer> ratingMap = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(i + " Star", 0);
        }

        int bucketUnder2m = 0;
        int bucket2to3m = 0;
        int bucket3to4m = 0;
        int bucket4to5m = 0;
        int bucket5mPlus = 0;

        for (LibraryItem item : items) {
            if (item.getGenre() != null && !item.getGenre().isBlank()) {
                genreMap.put(item.getGenre(), genreMap.getOrDefault(item.getGenre(), 0) + 1);
            }
            if (item.getArtistName() != null && !item.getArtistName().isBlank()) {
                artistMap.put(item.getArtistName(), artistMap.getOrDefault(item.getArtistName(), 0) + 1);
            }
            if (item.getReleaseDate() != null) {
                String year = String.valueOf(item.getReleaseDate().getYear());
                yearMap.put(year, yearMap.getOrDefault(year, 0) + 1);
            }
            if (item.getUserRating() != null && item.getUserRating() >= 1 && item.getUserRating() <= 5) {
                sumRating += item.getUserRating();
                ratingCount++;
                String key = item.getUserRating() + " Star";
                ratingMap.put(key, ratingMap.getOrDefault(key, 0) + 1);
            }
            if (item.getTrackCount() != null && item.getTrackCount() > 0) {
                long durationSeconds = item.getTrackCount() * 180L;
                sumDurationMs += durationSeconds * 1000;
                durationCount++;

                if (durationSeconds < 120) bucketUnder2m++;
                else if (durationSeconds < 180) bucket2to3m++;
                else if (durationSeconds < 240) bucket3to4m++;
                else if (durationSeconds < 300) bucket4to5m++;
                else bucket5mPlus++;
            } else {
                bucket3to4m++;
            }
        }

        double avgRating = ratingCount > 0 ? (double) Math.round((sumRating / ratingCount) * 10) / 10 : 0.0;
        double avgDurationSec = durationCount > 0 ? (sumDurationMs / durationCount) / 1000 : 210.0;

        Map<String, Integer> topArtists = artistMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        List<AnalyticsResponse.DurationBucket> histogram = List.of(
                new AnalyticsResponse.DurationBucket("<2m", bucketUnder2m),
                new AnalyticsResponse.DurationBucket("2-3m", bucket2to3m),
                new AnalyticsResponse.DurationBucket("3-4m", bucket3to4m),
                new AnalyticsResponse.DurationBucket("4-5m", bucket4to5m),
                new AnalyticsResponse.DurationBucket("5m+", bucket5mPlus)
        );

        return AnalyticsResponse.builder()
                .totalTracks(totalTracks)
                .averageRating(avgRating)
                .averageDurationSeconds(avgDurationSec)
                .tracksByGenre(genreMap)
                .tracksByArtist(topArtists)
                .tracksByReleaseYear(yearMap)
                .ratingDistribution(ratingMap)
                .durationHistogram(histogram)
                .build();
    }
}
