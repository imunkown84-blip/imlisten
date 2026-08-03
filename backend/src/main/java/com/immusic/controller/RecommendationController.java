package com.immusic.controller;

import com.immusic.dto.recommendation.RecommendationResponse;
import com.immusic.service.RecommendationService;
import com.immusic.util.CurrentUser;
import lombok.RequiredArgsConstructor;
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
    public RecommendationResponse getRecommendations() {
        return recommendationService.getRecommendations(currentUser.getUserId());
    }
}
