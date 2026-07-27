package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.LogEntry;
import com.antoniosandu.mindtrench.entity.enums.LogType;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private void addLog(
            Game game,
            LogType type,
            boolean playersSide,
            String message
    ){
        game.getLogs().add(
                new LogEntry(
                        game.getTurnNumber(),
                        type,
                        playersSide,
                        message
                )
        );
    }

    public void action(
            Game game,
            boolean playerSide,
            String message
    ) {
        addLog(game, LogType.ACTION, playerSide, message);
    }

    public void itemFound(
            Game game,
            boolean playerSide,
            String message
    ) {
        addLog(game, LogType.ITEM_FOUND, playerSide, message);
    }

    public void itemUsed(
            Game game,
            boolean playerSide,
            String message
    ) {
        addLog(game, LogType.ITEM_USED, playerSide, message);
    }

    public void itemTriggered(
            Game game,
            boolean playerSide,
            String message
    ) {
        addLog(game, LogType.ITEM_TRIGGERED, playerSide, message);
    }

    public void system(
            Game game,
            boolean playerSide,
            String message
    ) {
        addLog(game, LogType.SYSTEM, playerSide, message);
    }
}