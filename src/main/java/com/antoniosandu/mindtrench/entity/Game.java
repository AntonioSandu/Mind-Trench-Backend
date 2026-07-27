package com.antoniosandu.mindtrench.entity;

import com.antoniosandu.mindtrench.entity.enums.GameMode;
import com.antoniosandu.mindtrench.entity.enums.ItemType;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameMode mode;

    @Column(nullable = false)
    private int turnNumber = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "health",
                    column = @Column(name = "player_health")
            ),
            @AttributeOverride(
                    name = "currentNode",
                    column = @Column(name = "player_current_node")
            )
    })
    private CharacterState playerState;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<ItemType> playerInventory = new ArrayList<>();

    @ElementCollection
    private List<StatusEffect> playerStatusEffects = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "health",
                    column = @Column(name = "boss_health")
            ),
            @AttributeOverride(
                    name = "currentNode",
                    column = @Column(name = "boss_current_node")
            )
    })
    private CharacterState bossState;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<ItemType> bossInventory = new ArrayList<>();

    @ElementCollection
    private List<StatusEffect> bossStatusEffects = new ArrayList<>();

    @ElementCollection
    private List<MapEffect> mapEffects = new ArrayList<>();

    @ElementCollection
    private List<LogEntry> logs = new ArrayList<>();

    @Column(nullable = false)
    private int endlessScore = 0;

    public Game() {
    }

    public Game(
            User user,
            GameMode mode
    ) {
        this.user = user;
        this.mode = mode;
        this.turnNumber = 1;
        this.endlessScore = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CharacterState getPlayer() {
        return playerState;
    }

    public void setPlayer(CharacterState player) {
        this.playerState = player;
    }

    public CharacterState getBoss() {
        return bossState;
    }

    public void setBoss(CharacterState boss) {
        this.bossState = boss;
    }

    public List<ItemType> getPlayerInventory() {
        return playerInventory;
    }

    public List<ItemType> getBossInventory() {
        return bossInventory;
    }

    public List<StatusEffect> getPlayerStatusEffects() {
        return playerStatusEffects;
    }

    public List<StatusEffect> getBossStatusEffects() {
        return bossStatusEffects;
    }

    public List<MapEffect> getMapEffects() {
        return mapEffects;
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public int getEndlessScore() {
        return endlessScore;
    }

    public void setEndlessScore(int endlessScore) {
        this.endlessScore = endlessScore;
    }

}
