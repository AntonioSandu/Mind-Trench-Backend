package com.antoniosandu.mindtrench.controller;

import com.antoniosandu.mindtrench.dto.response.LeaderboardResponse;
import com.antoniosandu.mindtrench.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(
            LeaderboardService leaderboardService) {

        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {

        return ResponseEntity.ok(
                leaderboardService.getLeaderboard());
    }
}