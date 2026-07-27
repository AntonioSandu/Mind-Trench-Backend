package com.antoniosandu.mindtrench.mapper;

import com.antoniosandu.mindtrench.dto.response.GameResponse;
import com.antoniosandu.mindtrench.dto.response.GameStateResponse;
import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.LogEntry;
import com.antoniosandu.mindtrench.entity.MapEffect;

import java.util.ArrayList;
import java.util.List;

public class GameMapper {

    public static GameResponse toResponse(Game game) {

        GameResponse response = new GameResponse();

        response.setId(game.getId());
        response.setMode(game.getMode());
        response.setTurnNumber(game.getTurnNumber());
        response.setCreatedAt(game.getCreatedAt());

        return response;
    }

    public static GameStateResponse toStateResponse(Game game){
        GameStateResponse response = new GameStateResponse();

        response.setPlayerHealth(
                game.getPlayer().getHealth()
        );
        response.setBossHealth(
                game.getBoss().getHealth()
        );
        response.setPlayerNode(
                game.getPlayer().getCurrentNode()
        );
        response.setPlayerInventory(
                new ArrayList<>(game.getPlayerInventory())
        );
        response.setPlayerStatusEffects(
                new ArrayList<>(game.getPlayerStatusEffects())
        );
        response.setPlayerVisibleMapEffects(
                new ArrayList<>(
                game.getMapEffects()
                        .stream()
                        .filter(MapEffect::isBelongsToPlayer)
                        .toList()
                )
        );
        response.setLogs(
                new ArrayList<>(
                    game.getLogs()
                            .stream()
                            .filter(LogEntry::getPlayerSide)
                            .toList()
                )
        );
        response.setTurnNumber(
                game.getTurnNumber()
        );
        response.setMode(
                game.getMode()
        );
        response.setEndlessScore(
                game.getEndlessScore()
        );

        return response;
    }
}