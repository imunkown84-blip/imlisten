package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.SearchResultDto;
import com.musiccatalog.app.exception.UpstreamApiException;
import com.musiccatalog.app.external.ItunesSearchResponse;
import com.musiccatalog.app.external.ItunesTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItunesService {

    private final RestTemplate restTemplate;

    @Value("${app.itunes.base-url}")
    private String baseUrl;

    /**
     * Searches the public iTunes catalog. This app focuses on the "song" entity as its
     * primary use case, but any entity type supported by iTunes can be passed through.
     */
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

        try {
            ItunesSearchResponse response = restTemplate.getForObject(uri, ItunesSearchResponse.class);
            if (response == null || response.results() == null) {
                return List.of();
            }
            return response.results().stream()
                    .filter(Objects::nonNull)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (RestClientException ex) {
            Throwable root = ex;
            while (root.getCause() != null) root = root.getCause();
            log.error("iTunes API call failed for query='{}' uri='{}': {}", query, uri, root.toString(), ex);
            throw new UpstreamApiException("iTunes API error: " + root.getClass().getSimpleName() + " - " + root.getMessage(), ex);
        }
    }

    /** Used internally by the recommendation engine to pull candidate tracks for a genre/artist. */
    public List<SearchResultDto> searchTopByTerm(String term, int limit) {
        return search(term, "song", limit).stream()
                .sorted(Comparator.comparing(SearchResultDto::title))
                .toList();
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
