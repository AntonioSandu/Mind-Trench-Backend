package com.antoniosandu.mindtrench.entity;

import com.antoniosandu.mindtrench.entity.enums.LogType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class LogEntry {

    @Column(nullable = false)
    private int turnNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType type;

    @Column(nullable = false)
    private boolean playerSide;

    @Column(nullable = false)
    private String message;

    public LogEntry() {
    }

    public LogEntry(
            int turnNumber,
            LogType type,
            boolean playerSide,
            String message
    ) {
        this.turnNumber = turnNumber;
        this.type = type;
        this.playerSide=playerSide;
        this.message = message;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public boolean getPlayerSide() { return playerSide;}

    public void setPlayerSide(boolean playerSide){
        this.playerSide= playerSide;
    }

    public String getMessage() {
        return message;
    }
}