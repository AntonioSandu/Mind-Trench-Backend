package com.antoniosandu.mindtrench.entity;

import com.antoniosandu.mindtrench.entity.enums.MapEffectType;
import com.antoniosandu.mindtrench.game.map.NodeId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class MapEffect {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MapEffectType type;

    @Enumerated(EnumType.STRING)
    private NodeId firstNode;

    @Enumerated(EnumType.STRING)
    private NodeId secondNode;

    @Column(nullable = false)
    private boolean belongsToPlayer;

    public MapEffect() {
    }

    public MapEffect(
            MapEffectType type,
            NodeId firstNode,
            NodeId secondNode,
            boolean belongsToPlayer
    ) {
        this.type = type;
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.belongsToPlayer = belongsToPlayer;
    }

    public MapEffectType getType() {
        return type;
    }

    public NodeId getFirstNode() {
        return firstNode;
    }

    public NodeId getSecondNode() {
        return secondNode;
    }

    public boolean isBelongsToPlayer() {
        return belongsToPlayer;
    }
}