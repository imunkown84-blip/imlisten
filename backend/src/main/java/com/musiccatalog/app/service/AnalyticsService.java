package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.AnalyticsResponse;
import com.musiccatalog.app.model.LibraryItem;
import com.musiccatalog.app.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Powers the analytics dashboard. Every chart in the frontend maps to one field here:
 *  - tracksByGenre        -> Bar chart
 *  - tracksByArtist       -> Horizontal bar chart (top artists)
 *  - tracksByReleaseYear  -> Line chart (releases by year)
 *  - ratingDistribution   -> Pie / donut chart
 *  - durationHistogram    -> Histogram (track length buckets)
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LibraryItemRepository repository;

    public AnalyticsResponse getAnalytics(Long userId) {
        List<LibraryItem> items = repository.findByUserId(userId);

        long total = items.size();

        double avgRating = items.stream()
                .filter(i -> i.getUserRating() != null)
                .mapToInt(LibraryItem::getUserRating)
                .average()
                .orElse(0.0);

        double avgDurationSeconds = items.stream()
                .filter(i -> i.getDurationMillis() != null)
                .mapToLong(LibraryItem::getDurationMillis)
                .average()
                .orElse(0.0) / 1000.0;

        Map<String, Long> byGenre = items.stream()
                .map(i -> emptyToUnknown(i.getGenre()))
                .collect(Collectors.groupingBy(g -> g, LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> byArtist = items.stream()
                .collect(Collectors.groupingBy(LibraryItem::getArtistName, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        Map<Integer, Long> byYear = items.stream()
                .filter(i -> i.getReleaseDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getReleaseDate().getYear(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> ratingDist = items.stream()
                .map(i -> i.getUserRating() == null ? "Unrated" : i.getUserRating() + " star")
                .collect(Collectors.groupingBy(r -> r, LinkedHashMap::new, Collectors.counting()));

        List<AnalyticsResponse.DurationBucket> durationHistogram = buildDurationHistogram(items);

        return new AnalyticsResponse(total, avgRating, avgDurationSeconds, byGenre, byArtist, byYear, ratingDist, durationHistogram);
    }

    private List<AnalyticsResponse.DurationBucket> buildDurationHistogram(List<LibraryItem> items) {
        // Buckets in seconds: <2min, 2-3min, 3-4min, 4-5min, 5min+
        int[] bucketCounts = new int[5];
        for (LibraryItem item : items) {
            if (item.getDurationMillis() == null) continue;
            double seconds = item.getDurationMillis() / 1000.0;
            if (seconds < 120) bucketCounts[0]++;
            else if (seconds < 180) bucketCounts[1]++;
            else if (seconds < 240) bucketCounts[2]++;
            else if (seconds < 300) bucketCounts[3]++;
            else bucketCounts[4]++;
        }
        String[] labels = {"<2 min", "2-3 min", "3-4 min", "4-5 min", "5+ min"};
        return java.util.stream.IntStream.range(0, labels.length)
                .mapToObj(i -> new AnalyticsResponse.DurationBucket(labels[i], bucketCounts[i]))
                .toList();
    }

    private String emptyToUnknown(String genre) {
        return (genre == null || genre.isBlank()) ? "Unknown" : genre;
    }
}
