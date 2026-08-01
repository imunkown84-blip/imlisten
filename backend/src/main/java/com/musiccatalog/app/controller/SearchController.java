package com.musiccatalog.app.controller;

import com.musiccatalog.app.dto.SearchResultDto;
import com.musiccatalog.app.service.ItunesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final ItunesService itunesService;

    /**
     * Proxies the public iTunes Search API. Defaults to entity=song since that's
     * this app's chosen focus, but accepts album/musicArtist too.
     */
    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "song") String type,
            @RequestParam(required = false, defaultValue = "25") int limit
    ) {
        return ResponseEntity.ok(itunesService.search(query, type, limit));
    }
}
