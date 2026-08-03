package com.immusic.controller;

import com.immusic.dto.analytics.AnalyticsResponse;
import com.immusic.service.AnalyticsService;
import com.immusic.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    @GetMapping
    public AnalyticsResponse getAnalytics() {
        return analyticsService.getAnalytics(currentUser.getUserId());
    }
}
