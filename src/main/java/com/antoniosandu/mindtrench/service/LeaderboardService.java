package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.dto.response.LeaderboardResponse;
import com.antoniosandu.mindtrench.mapper.UserMapper;
import com.antoniosandu.mindtrench.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public List<LeaderboardResponse> getLeaderboard() {

        return userRepository
                .findTop10ByBestEndlessScoreGreaterThanOrderByBestEndlessScoreDesc(0)
                .stream()
                .map(UserMapper::toLeaderboardResponse)
                .toList();
    }
}
