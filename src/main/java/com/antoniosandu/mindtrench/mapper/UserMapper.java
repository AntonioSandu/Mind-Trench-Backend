package com.antoniosandu.mindtrench.mapper;

import com.antoniosandu.mindtrench.dto.response.AuthResponse;
import com.antoniosandu.mindtrench.dto.response.LeaderboardResponse;
import com.antoniosandu.mindtrench.entity.User;

public class UserMapper {

    public static AuthResponse toAuthResponse(User user) {

        AuthResponse response = new AuthResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setBestEndlessScore(user.getBestEndlessScore());

        return response;
    }

    public static LeaderboardResponse toLeaderboardResponse(User user) {

        LeaderboardResponse response = new LeaderboardResponse();

        response.setUsername(user.getUsername());
        response.setBestEndlessScore(user.getBestEndlessScore());

        return response;
    }
}
