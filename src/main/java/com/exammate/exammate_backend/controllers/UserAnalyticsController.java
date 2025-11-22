package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.UserAnalyticsResponse;
import com.exammate.exammate_backend.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class UserAnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user analytics (JSON)")
    public UserAnalyticsResponse getUserAnalytics(@PathVariable String userId) {
        return analyticsService.userAnalytics(userId);
    }
}

