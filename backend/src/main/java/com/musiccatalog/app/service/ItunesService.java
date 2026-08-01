package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.SearchResultDto;
import com.musiccatalog.app.exception.UpstreamApiException;
import com.musiccatalog.app.external.ItunesSearchResponse;
import com.musiccatalog.app.external.ItunesTrack;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItunesService {

    private final RestTemplate restTemplate;

    @Value("${app.itunes.base-url}")
    private String baseUrl;

    @Value("${app.itunes.max-requests-per-minute:15}")
    private int maxRequestsPerMinute;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 2000;

    private Semaphore rateLimiter;
    private ScheduledExecutorService permitScheduler;

    @PostConstruct
    void initRateLimiter() {
        rateLimiter = new Semaphore(maxRequestsPerMinute);
        // Replenish one permit every (60 / maxRequestsPerMinute) seconds
        long replenishIntervalMs = (60_000L / maxRequestsPerMinute);
        permitScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "itunes-rate-limiter");
            t.setDaemon(true);
            return t;
        });
        permitScheduler.scheduleAtFixedRate(() -> {
            int currentPermits = rateLimiter.availablePermits();
            if (currentPermits < maxRequestsPerMinute) {
                rateLimiter.release();
            }
        }, replenishIntervalMs, replenishIntervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Searches the public iTunes catalog. This app focuses on the "song" entity as its
     * primary use case, but any entity type supported by iTunes can be passed through.
     *
     * Results are cached for 5 minutes (configurable) to reduce API pressure.
     * Requests are rate-limited and retried with exponential backoff on 429 responses.
     */
    @Cacheable(value = "itunesSearch", key = "#query + '|' + #entity + '|' + #limit")
    public List<SearchResultDto> search(String query, String entity, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query parameter is required");
        }
        String safeEntity = (entity == null || entity.isBlank()) ? "song" : entity;
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        java.net.URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("term", query)
                .queryParam("entity", safeEntity)
                .queryParam("limit", safeLimit)
                .build()
                .toUri();

        return executeWithRetry(query, uri);
    }

    /** Used internally by the recommendation engine to pull candidate tracks for a genre/artist. */
    public List<SearchResultDto> searchTopByTerm(String term, int limit) {
        return search(term, "song", limit).stream()
                .sorted(Comparator.comparing(SearchResultDto::title))
                .toList();
    }

    /**
     * Executes the iTunes API call with rate limiting and retry-with-backoff on 429s.
     */
    private List<SearchResultDto> executeWithRetry(String query, java.net.URI uri) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            // Acquire a rate-limit permit (blocks if quota is exhausted)
            try {
                if (!rateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                    log.warn("Rate limiter timed out for query='{}' — too many concurrent requests", query);
                    throw new UpstreamApiException(
                            "Search is temporarily busy — please wait a moment and try again", null, true);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new UpstreamApiException("Request interrupted", ie);
            }

            try {
                ItunesSearchResponse response = restTemplate.getForObject(uri, ItunesSearchResponse.class);
                if (response == null || response.results() == null) {
                    return List.of();
                }
                return response.results().stream()
                        .filter(Objects::nonNull)
                        .map(this::toDto)
                        .collect(Collectors.toList());

            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                        || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                    if (attempt < MAX_RETRIES) {
                        long backoff = INITIAL_BACKOFF_MS * (1L << attempt); // 2s, 4s, 8s
                        log.warn("iTunes API returned {} for query='{}'. Retrying in {}ms (attempt {}/{})",
                                ex.getStatusCode().value(), query, backoff, attempt + 1, MAX_RETRIES);
                        sleep(backoff);
                        continue;
                    }
                    log.error("iTunes API rate limit exhausted after {} retries for query='{}'", MAX_RETRIES, query);
                    throw new UpstreamApiException(
                            "Music catalog is temporarily unavailable — Apple's rate limit reached. Please try again in a minute.",
                            ex, true);
                }
                // Non-429/403 client error — don't retry
                log.error("iTunes API client error for query='{}' uri='{}': {}", query, uri, ex.getMessage(), ex);
                throw new UpstreamApiException("iTunes API error: " + ex.getStatusCode().value() + " - " + ex.getStatusText(), ex);

            } catch (RestClientException ex) {
                Throwable root = ex;
                while (root.getCause() != null) root = root.getCause();
                log.error("iTunes API call failed for query='{}' uri='{}': {}", query, uri, root.toString(), ex);
                throw new UpstreamApiException("iTunes API error: " + root.getClass().getSimpleName() + " - " + root.getMessage(), ex);
            }
        }
        // Should never reach here
        throw new UpstreamApiException("iTunes API request failed after all retries", null, true);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private SearchResultDto toDto(ItunesTrack t) {
        return new SearchResultDto(
                t.trackId(),
                t.trackName(),
                t.artistName(),
                t.collectionName(),
                t.primaryGenreName(),
                t.releaseDate(),
                t.trackTimeMillis(),
                t.artworkUrl100(),
                t.trackPrice(),
                t.previewUrl()
        );
    }
}
