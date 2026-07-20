package com.antoniosandu.mindtrench.game.boss;

import com.antoniosandu.mindtrench.dto.request.TurnRequest;
import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.StatusEffect;
import com.antoniosandu.mindtrench.entity.enums.StatusEffectType;
import com.antoniosandu.mindtrench.game.action.ActionType;
import com.antoniosandu.mindtrench.game.map.NodeId;

import java.util.List;

public final class BossBehaviour {

    private BossBehaviour() {}

    private static boolean hasStatus(
            List<StatusEffect> effects,
            StatusEffectType type
    ) {
        return effects.stream()
                .anyMatch(effect ->
                        effect.getType() == type
                );
    }

    public static TurnRequest generateMove(Game game) {

        if (hasStatus(game.getBossStatusEffects(), StatusEffectType.TRAPPED)) {
            return new TurnRequest(ActionType.STAY_STILL, List.of());
        }

        NodeId current =
                game.getBoss().getCurrentNode();

        NodeId target = switch (current) {

            case A -> NodeId.C;
            case C -> NodeId.D;
            case D -> NodeId.E;
            case E -> NodeId.G;
            case G -> NodeId.F;
            case F -> NodeId.B;
            case B -> NodeId.A;
        };

        return new TurnRequest(
                ActionType.MOVE,
                List.of(target)
        );
    }

}