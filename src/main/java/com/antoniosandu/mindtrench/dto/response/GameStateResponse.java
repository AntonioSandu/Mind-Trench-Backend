package com.antoniosandu.mindtrench.dto.response;

import com.antoniosandu.mindtrench.dto.response.enums.GameResult;
import com.antoniosandu.mindtrench.entity.LogEntry;
import com.antoniosandu.mindtrench.entity.MapEffect;
import com.antoniosandu.mindtrench.entity.StatusEffect;
import com.antoniosandu.mindtrench.entity.enums.GameMode;
import com.antoniosandu.mindtrench.entity.enums.ItemType;
import com.antoniosandu.mindtrench.game.map.NodeId;

import java.util.List;

public class GameStateResponse {
    private int playerHealth;
    private int bossHealth;
    private NodeId playerNode;
    private List<ItemType> playerInventory;
    private List<StatusEffect> playerStatusEffects;
    private List<MapEffect> playerVisibleMapEffects;
    private List<LogEntry> logs;
    private int turnNumber;
    private GameMode mode;
    private int endlessScore;
    private GameResult result;
    private boolean gameOver;


    public GameStateResponse(){}

    public GameStateResponse(int playerHealth, int bossHealth, NodeId playerNode,
                             List<ItemType> playerInventory, List<StatusEffect> playerStatusEffects,
                             List<MapEffect> playerVisibleMapEffects, List<LogEntry> logs,
                             int turnNumber, GameMode mode, int endlessScore, GameResult result, boolean gameOver) {
        this.playerHealth = playerHealth;
        this.bossHealth = bossHealth;
        this.playerNode = playerNode;
        this.playerInventory = playerInventory;
        this.playerStatusEffects = playerStatusEffects;
        this.playerVisibleMapEffects = playerVisibleMapEffects;
        this.logs = logs;
        this.turnNumber = turnNumber;
        this.mode = mode;
        this.endlessScore = endlessScore;
        this.result = result;
        this.gameOver = gameOver;
    }

    // Getter e Setter
    public int getPlayerHealth() {
        return playerHealth;
    }

    public void setPlayerHealth(int playerHealth) {
        this.playerHealth = playerHealth;
    }

    public int getBossHealth() {
        return bossHealth;
    }

    public void setBossHealth(int bossHealth) {
        this.bossHealth = bossHealth;
    }

    public NodeId getPlayerNode() {
        return playerNode;
    }

    public void setPlayerNode(NodeId playerNode) {
        this.playerNode = playerNode;
    }

    public List<ItemType> getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(List<ItemType> playerInventory) {
        this.playerInventory = playerInventory;
    }

    public List<StatusEffect> getPlayerStatusEffects() {
        return playerStatusEffects;
    }

    public void setPlayerStatusEffects(List<StatusEffect> playerStatusEffects) {
        this.playerStatusEffects = playerStatusEffects;
    }

    public List<MapEffect> getPlayerVisibleMapEffects() {
        return playerVisibleMapEffects;
    }

    public void setPlayerVisibleMapEffects(List<MapEffect> playerVisibleMapEffects) {
        this.playerVisibleMapEffects = playerVisibleMapEffects;
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntry> logs) {
        this.logs = logs;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public int getEndlessScore() {
        return endlessScore;
    }

    public void setEndlessScore(int endlessScore) {
        this.endlessScore = endlessScore;
    }

    public GameResult getResult() {return result;}

    public void setResult(GameResult result){
        this.result= result;
    }

    public boolean isGameOver() {return gameOver;}

    public void setGameOver(boolean gameOver){
        this.gameOver = gameOver;
    }
}