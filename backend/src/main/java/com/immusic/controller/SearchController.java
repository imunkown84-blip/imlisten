package com.immusic.controller;

import com.immusic.dto.search.AlbumSearchResult;
import com.immusic.dto.search.SearchResponse;
import com.immusic.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public List<AlbumSearchResult> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "song") String type
    ) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        SearchResponse response = searchService.searchCatalog(query, type);
        return response != null && response.getResults() != null ? response.getResults() : List.of();
    }
}
