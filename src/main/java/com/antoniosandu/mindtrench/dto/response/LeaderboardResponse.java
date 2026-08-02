package com.antoniosandu.mindtrench.dto.response;

public class LeaderboardResponse {

    private String username;
    private int bestEndlessScore;

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public int getBestEndlessScore(){
        return bestEndlessScore;
    }

    public void setBestEndlessScore(int bestEndlessScore) {
        this.bestEndlessScore = bestEndlessScore;
    }
}
