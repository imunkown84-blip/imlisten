package com.immusic.service;

import com.immusic.dto.search.AlbumSearchResult;
import com.immusic.dto.search.SearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private static final String ITUNES_SEARCH_URL = "https://itunes.apple.com/search";
    private static final DateTimeFormatter ITUNES_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .defaultHeader("Accept", "application/json")
            .build();

    @Cacheable(value = "itunesSearch", key = "#query.toLowerCase() + '_' + #type.toLowerCase()")
    public SearchResponse searchCatalog(String query, String type) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder()
                    .query(query)
                    .resultCount(0)
                    .results(new ArrayList<>())
                    .build();
        }

        String entity = "song";
        if ("album".equalsIgnoreCase(type)) {
            entity = "album";
        } else if ("artist".equalsIgnoreCase(type)) {
            entity = "musicArtist";
        }

        List<AlbumSearchResult> results = new ArrayList<>();
        try {
            java.net.URI uri = UriComponentsBuilder.fromHttpUrl(ITUNES_SEARCH_URL)
                    .queryParam("term", query)
                    .queryParam("entity", entity)
                    .queryParam("limit", 25)
                    .build()
                    .encode()
                    .toUri();

            JsonNode root = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);

            if (root != null && root.has("results")) {
                for (JsonNode item : root.get("results")) {
                    results.add(mapToResult(item));
                }
            }
        } catch (Exception ex) {
            log.error("Failed to fetch search results from iTunes API for query='{}': {}", query, ex.getMessage(), ex);
        }

        return SearchResponse.builder()
                .query(query)
                .resultCount(results.size())
                .results(results)
                .build();
    }

    public SearchResponse searchAlbums(String query) {
        return searchCatalog(query, "song");
    }

    private AlbumSearchResult mapToResult(JsonNode item) {
        long catalogId = item.has("trackId") && !item.get("trackId").isNull()
                ? item.path("trackId").asLong()
                : item.path("collectionId").asLong(item.path("artistId").asLong(0L));

        String title = item.has("trackName") && !item.get("trackName").isNull()
                ? item.path("trackName").asText()
                : item.path("collectionName").asText(item.path("artistName").asText(""));

        return AlbumSearchResult.builder()
                .appleCatalogId(catalogId)
                .title(title)
                .artistName(item.path("artistName").asText(""))
                .collectionName(item.has("collectionName") && !item.get("collectionName").isNull() ? item.path("collectionName").asText() : null)
                .genre(item.has("primaryGenreName") && !item.get("primaryGenreName").isNull() ? item.path("primaryGenreName").asText() : null)
                .releaseDate(parseReleaseDate(item.has("releaseDate") && !item.get("releaseDate").isNull() ? item.path("releaseDate").asText() : null))
                .trackCount(item.has("trackCount") && !item.get("trackCount").isNull() ? item.get("trackCount").asInt() : null)
                .durationMillis(item.has("trackTimeMillis") && !item.get("trackTimeMillis").isNull() ? item.get("trackTimeMillis").asLong() : null)
                .artworkUrl(item.has("artworkUrl100") && !item.get("artworkUrl100").isNull() ? item.path("artworkUrl100").asText() : null)
                .previewUrl(item.has("previewUrl") && !item.get("previewUrl").isNull() ? item.path("previewUrl").asText() : null)
                .build();
    }

    private LocalDate parseReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank() || releaseDate.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(releaseDate.substring(0, 10), ITUNES_DATE_FORMAT);
        } catch (Exception ex) {
            log.debug("Unable to parse release date: {}", releaseDate);
            return null;
        }
    }
}
