package com.antoniosandu.mindtrench.entity;

import com.antoniosandu.mindtrench.game.map.NodeId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class CharacterState {

    @Column(nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeId currentNode;

    public CharacterState() {
    }

    public CharacterState(
            int health,
            NodeId currentNode
    ) {
        this.health = health;
        this.currentNode = currentNode;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public NodeId getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(NodeId currentNode) {
        this.currentNode = currentNode;
    }
}