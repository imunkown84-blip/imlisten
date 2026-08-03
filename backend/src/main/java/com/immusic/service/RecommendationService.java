package com.immusic.service;

import com.immusic.dto.recommendation.RecommendationResponse;
import com.immusic.dto.search.AlbumSearchResult;
import com.immusic.dto.search.SearchResponse;
import com.immusic.entity.LibraryItem;
import com.immusic.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final LibraryItemRepository libraryItemRepository;
    private final SearchService searchService;

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(UUID userId) {
        List<LibraryItem> libraryItems = libraryItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (libraryItems.isEmpty()) {
            return RecommendationResponse.builder()
                    .basedOn("Add songs to your library to receive personalized recommendations.")
                    .recommendations(new ArrayList<>())
                    .build();
        }

        Set<Long> savedCatalogIds = libraryItems.stream()
                .map(LibraryItem::getAppleCatalogId)
                .collect(Collectors.toSet());

        Map<String, Double> genreWeights = new HashMap<>();
        Map<String, Double> artistWeights = new HashMap<>();

        for (LibraryItem item : libraryItems) {
            double weight = item.getUserRating() != null ? item.getUserRating() : 3.0;
            if (item.getGenre() != null && !item.getGenre().isBlank()) {
                genreWeights.put(item.getGenre(), genreWeights.getOrDefault(item.getGenre(), 0.0) + weight);
            }
            if (item.getArtistName() != null && !item.getArtistName().isBlank()) {
                artistWeights.put(item.getArtistName(), artistWeights.getOrDefault(item.getArtistName(), 0.0) + (weight * 2.0));
            }
        }

        String topGenre = genreWeights.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Pop");

        String topArtist = artistWeights.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String basedOnMsg = topArtist != null
                ? "Based on your top artist " + topArtist + " & preferred genre " + topGenre
                : "Based on your preferred genre " + topGenre;

        Set<String> searchTerms = new HashSet<>();
        if (topArtist != null) searchTerms.add(topArtist);
        searchTerms.add(topGenre);

        List<RecommendationResponse.RecommendedTrack> recs = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();

        for (String term : searchTerms) {
            try {
                SearchResponse response = searchService.searchCatalog(term, "song");
                if (response != null && response.getResults() != null) {
                    for (AlbumSearchResult track : response.getResults()) {
                        if (savedCatalogIds.contains(track.getAppleCatalogId()) || addedIds.contains(track.getAppleCatalogId())) {
                            continue;
                        }

                        double score = 1.0;
                        String reason = "Popular track in " + (track.getGenre() != null ? track.getGenre() : topGenre);

                        if (topArtist != null && topArtist.equalsIgnoreCase(track.getArtistName())) {
                            score += 5.0;
                            reason = "Recommended because you enjoy " + topArtist;
                        } else if (track.getGenre() != null && genreWeights.containsKey(track.getGenre())) {
                            score += 3.0;
                            reason = "Matches your favorite genre " + track.getGenre();
                        }

                        recs.add(RecommendationResponse.RecommendedTrack.builder()
                                .appleCatalogId(track.getAppleCatalogId())
                                .title(track.getTitle())
                                .artistName(track.getArtistName())
                                .genre(track.getGenre())
                                .artworkUrl(track.getArtworkUrl())
                                .previewUrl(track.getPreviewUrl())
                                .reason(reason)
                                .score(score)
                                .build());

                        addedIds.add(track.getAppleCatalogId());
                        if (recs.size() >= 12) break;
                    }
                }
            } catch (Exception ex) {
                log.debug("Error fetching candidate recommendations for term {}: {}", term, ex.getMessage());
            }
            if (recs.size() >= 12) break;
        }

        recs.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return RecommendationResponse.builder()
                .basedOn(basedOnMsg)
                .recommendations(recs)
                .build();
    }
}
