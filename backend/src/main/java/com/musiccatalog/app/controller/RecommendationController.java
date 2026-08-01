package com.musiccatalog.app.controller;

import com.musiccatalog.app.dto.RecommendationResponse;
import com.musiccatalog.app.security.CurrentUser;
import com.musiccatalog.app.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations() {
        return ResponseEntity.ok(recommendationService.recommend(currentUser.id()));
    }
}
