package com.antoniosandu.mindtrench.entity;

import com.antoniosandu.mindtrench.entity.enums.StatusEffectType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class StatusEffect {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEffectType type;

    @Column(nullable = false)
    private int remainingTurns;

    public StatusEffect() {
    }

    public StatusEffect(
            StatusEffectType type,
            int remainingTurns
    ) {
        this.type = type;
        this.remainingTurns = remainingTurns;
    }

    public StatusEffectType getType() {
        return type;
    }

    public void setType(StatusEffectType type) {
        this.type = type;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }
}