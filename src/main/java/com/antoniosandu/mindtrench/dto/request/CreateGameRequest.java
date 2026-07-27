package com.antoniosandu.mindtrench.dto.request;

import com.antoniosandu.mindtrench.entity.enums.GameMode;
import jakarta.validation.constraints.NotNull;

public class CreateGameRequest {

    @NotNull
    private Long userId;

    @NotNull
    private GameMode mode;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }
}