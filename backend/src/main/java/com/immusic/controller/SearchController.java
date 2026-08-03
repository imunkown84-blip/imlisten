package com.immusic.controller;

import com.immusic.dto.search.SearchResponse;
import com.immusic.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResponse search(
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "album") String type
    ) {
        if (!"album".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Only type=album is supported");
        }
        return searchService.searchAlbums(query);
    }
}
