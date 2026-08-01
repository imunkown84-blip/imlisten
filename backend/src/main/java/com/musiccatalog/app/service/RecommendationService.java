package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.RecommendationResponse;
import com.musiccatalog.app.dto.SearchResultDto;
import com.musiccatalog.app.model.LibraryItem;
import com.musiccatalog.app.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI feature: "Recommendations".
 *
 * Approach — content-based filtering, not a black-box LLM call:
 *   1. Build a taste profile from the user's saved library: weighted genre affinity
 *      and favorite artists, with user_rating used to boost/downweight tracks.
 *   2. Pull fresh candidate tracks from the iTunes catalog by querying the user's
 *      top genres and top artists (related-artist style search).
 *   3. Score each candidate against the taste profile (genre match, artist match,
 *      novelty) and return the top N, excluding anything already saved.
 *
 * This keeps the feature fully self-contained (no external LLM API key required)
 * while still being genuinely personalized and explainable — each recommendation
 * carries a human-readable "reason".
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 12;
    private static final int CANDIDATE_POOL_PER_TERM = 15;

    private final LibraryItemRepository libraryItemRepository;
    private final ItunesService itunesService;

    public RecommendationResponse recommend(Long userId) {
        List<LibraryItem> library = libraryItemRepository.findByUserId(userId);

        if (library.isEmpty()) {
            return new RecommendationResponse(
                    "your library is empty — save a few songs first",
                    List.of()
            );
        }

        Map<String, Double> genreAffinity = buildGenreAffinity(library);
        Map<String, Double> artistAffinity = buildArtistAffinity(library);
        Set<Long> alreadySaved = library.stream().map(LibraryItem::getAppleCatalogId).collect(Collectors.toSet());

        List<String> seedTerms = pickSeedTerms(genreAffinity, artistAffinity);

        Map<Long, SearchResultDto> candidates = new LinkedHashMap<>();
        for (String term : seedTerms) {
            try {
                itunesService.searchTopByTerm(term, CANDIDATE_POOL_PER_TERM)
                        .forEach(track -> candidates.putIfAbsent(track.appleCatalogId(), track));
            } catch (Exception e) {
                log.warn("Recommendation candidate search failed for term '{}': {}", term, e.getMessage());
            }
        }

        List<RecommendationResponse.RecommendedTrack> scored = candidates.values().stream()
                .filter(c -> c.appleCatalogId() != null && !alreadySaved.contains(c.appleCatalogId()))
                .map(c -> score(c, genreAffinity, artistAffinity))
                .sorted(Comparator.comparingDouble(RecommendationResponse.RecommendedTrack::score).reversed())
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        String basedOn = "top genres: " + topKeys(genreAffinity, 3) + "; favorite artists: " + topKeys(artistAffinity, 3);
        return new RecommendationResponse(basedOn, scored);
    }

    private Map<String, Double> buildGenreAffinity(List<LibraryItem> library) {
        Map<String, Double> affinity = new HashMap<>();
        for (LibraryItem item : library) {
            String genre = (item.getGenre() == null || item.getGenre().isBlank()) ? "Unknown" : item.getGenre();
            double weight = ratingWeight(item.getUserRating());
            affinity.merge(genre, weight, Double::sum);
        }
        return affinity;
    }

    private Map<String, Double> buildArtistAffinity(List<LibraryItem> library) {
        Map<String, Double> affinity = new HashMap<>();
        for (LibraryItem item : library) {
            double weight = ratingWeight(item.getUserRating());
            affinity.merge(item.getArtistName(), weight, Double::sum);
        }
        return affinity;
    }

    /** Unrated defaults to neutral (1.0); ratings 1-5 scale the weight from 0.4x to 2.0x. */
    private double ratingWeight(Integer rating) {
        if (rating == null) return 1.0;
        return 0.4 + (rating - 1) * 0.4; // 1->0.4, 2->0.8, 3->1.2, 4->1.6, 5->2.0
    }

    private List<String> pickSeedTerms(Map<String, Double> genreAffinity, Map<String, Double> artistAffinity) {
        List<String> terms = new ArrayList<>();
        topKeys(genreAffinity, 3).forEach(g -> terms.add(g.equals("Unknown") ? "top hits" : g));
        terms.addAll(topKeys(artistAffinity, 3));
        return terms.stream().distinct().toList();
    }

    private RecommendationResponse.RecommendedTrack score(SearchResultDto c,
                                                            Map<String, Double> genreAffinity,
                                                            Map<String, Double> artistAffinity) {
        double genreScore = genreAffinity.getOrDefault(c.genre(), 0.0);
        double artistScore = artistAffinity.getOrDefault(c.artistName(), 0.0) * 1.5; // artist match weighted higher
        double total = genreScore + artistScore;

        String reason;
        if (artistScore > 0 && genreScore > 0) {
            reason = "You like " + c.artistName() + " and the " + c.genre() + " genre";
        } else if (artistScore > 0) {
            reason = "You already enjoy " + c.artistName();
        } else if (genreScore > 0) {
            reason = "Matches your taste for " + c.genre();
        } else {
            reason = "Popular pick you haven't saved yet";
        }

        return new RecommendationResponse.RecommendedTrack(
                c.appleCatalogId(), c.title(), c.artistName(), c.genre(), c.artworkUrl(), c.previewUrl(), reason, total
        );
    }

    private List<String> topKeys(Map<String, Double> affinity, int n) {
        return affinity.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }
}
