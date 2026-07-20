package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.dto.request.TurnRequest;
import com.antoniosandu.mindtrench.dto.response.GameStateResponse;
import com.antoniosandu.mindtrench.dto.response.enums.GameResult;
import com.antoniosandu.mindtrench.entity.*;
import com.antoniosandu.mindtrench.entity.enums.GameMode;
import com.antoniosandu.mindtrench.entity.enums.ItemType;
import com.antoniosandu.mindtrench.entity.enums.MapEffectType;
import com.antoniosandu.mindtrench.entity.enums.StatusEffectType;
import com.antoniosandu.mindtrench.exception.GameNotFoundException;
import com.antoniosandu.mindtrench.exception.InvalidTurnException;
import com.antoniosandu.mindtrench.game.action.ActionType;
import com.antoniosandu.mindtrench.game.boss.BossBehaviour;
import com.antoniosandu.mindtrench.game.map.MapDefinition;
import com.antoniosandu.mindtrench.game.map.NodeId;
import com.antoniosandu.mindtrench.mapper.GameMapper;
import com.antoniosandu.mindtrench.repository.GameRepository;
import org.springframework.stereotype.Service;
import java.util.Random;

import java.util.List;

@Service
public class GameEngineService {

    private final GameRepository gameRepository;
    private final LogService logService;

    GameEngineService(
            LogService logService,
            GameRepository gameRepository){
        this.logService = logService;
        this.gameRepository = gameRepository;
    }

    //VALIDATIONS
    private void validateTurnRequest(
            Game game,
            TurnRequest request
    ) {
        boolean trapped = hasStatus(
                game.getPlayerStatusEffects(),
                StatusEffectType.TRAPPED
        );

        if (trapped) {
            if (request.getActionType() != ActionType.STAY_STILL) {
                throw new InvalidTurnException(
                        "You are trapped and must stay still."
                );
            }

            if (!request.getTargetNodes().isEmpty()) {
                throw new InvalidTurnException(
                        "Stay still requires no target nodes."
                );
            }

            return;
        }

        if (request.getActionType() == ActionType.STAY_STILL) {
            throw new InvalidTurnException(
                    "You are not trapped, select another action to perform."
            );
        }

        int expectedSize = switch (request.getActionType()) {

            case SLEEP -> 0;

            case STRIKE -> 1;

            case MOVE -> hasStatus(
                    game.getPlayerStatusEffects(),
                    StatusEffectType.STICK
            )
                    ? 2
                    : 1;

            case STAY_STILL -> 0;
        };

        if (request.getTargetNodes().size() != expectedSize) {
            throw new InvalidTurnException(
                    "Expected " + expectedSize + " target nodes."
            );
        }

        if (request.getActionType() == ActionType.MOVE) {
            NodeId current =
                    game.getPlayer().getCurrentNode();
            for (NodeId next : request.getTargetNodes()) {
                if (!MapDefinition.areConnected(current, next)) {
                    throw new InvalidTurnException(
                            "Invalid movement path."
                    );
                }
                current = next;
            }
        }
    }

    public GameStateResponse executeTurn(
            Long gameId,
            Long userId,
            TurnRequest request
    ){
        Game game = gameRepository
                .findByIdAndUserId(gameId, userId)
                .orElseThrow(() ->
                        new GameNotFoundException(
                                "Game not found"
                        )
                );

        return executeTurn(
                game,
                request
        );
    }

    private GameStateResponse executeTurn(
            Game game,
            TurnRequest playerRequest
    ) {

        validateTurnRequest(game, playerRequest);

        TurnRequest bossRequest = generateBossAction(game);

        if (playerRequest.getActionType() == ActionType.MOVE) {
            resolveMove(game, playerRequest, true);
        }

        if (bossRequest.getActionType() == ActionType.MOVE) {
            resolveMove(game, bossRequest, false);
        }

        GameStateResponse response;

        response= resolveNapalmSecondHit(game, true);
        if (response != null) {
            return response;
        }
        response= resolveNapalmSecondHit(game, false);
        if (response != null) {
            return response;
        }

        if (playerRequest.getActionType() == ActionType.STRIKE) {
            response = resolveStrike(game, playerRequest, true);
            if (response != null) {
                return response;
            }
        }

        if (bossRequest.getActionType() == ActionType.STRIKE) {
            response = resolveStrike(game, bossRequest, false);
            if (response != null) {
                return response;
            }
        }

        if (playerRequest.getActionType() == ActionType.STAY_STILL) {
            resolveStayStill(game, true);
        }

        if (bossRequest.getActionType() == ActionType.STAY_STILL) {
            resolveStayStill(game, false);
        }

        if (playerRequest.getActionType() == ActionType.SLEEP) {
            resolveSleep(game, true);
        }

        if (bossRequest.getActionType() == ActionType.SLEEP) {
            resolveSleep(game, false);
        }

        resolvePersistentEffects(
                game,
                true,
                playerRequest
        );

        resolvePersistentEffects(
                game,
                false,
                bossRequest
        );

        game.setTurnNumber(
                game.getTurnNumber() + 1
        );

        logService.system(
                game,
                true,
                "It's now day " + game.getTurnNumber()
        );

        gameRepository.save(game);

        response = GameMapper.toStateResponse(game);

        response.setGameOver(false);
        response.setResult(null);

        return response;
    }

    private TurnRequest generateBossAction(Game game) {
        return BossBehaviour.generateMove(game);
    }

    private void resolveStayStill(
            Game game,
            boolean playerSide
    ) {
        List<StatusEffect> statuses = getStatuses(game, playerSide);
        removeStatus(
                statuses,
                StatusEffectType.TRAPPED
        );
        NodeId currentNode = getCharacter(game, playerSide).getCurrentNode();
        removeBeartrap(game, playerSide, currentNode);
        logService.action(
                game,
                playerSide,
                "You focus all your energy to set yourself free from the trap, this night you don nothing else"
        );
    }

    private void resolveMove(
            Game game,
            TurnRequest action,
            boolean playerSide
    ) {

        CharacterState character =
                getCharacter(game, playerSide);

        List<StatusEffect> statuses =
                getStatuses(game, playerSide);

        boolean stick = hasStatus(
                statuses,
                StatusEffectType.STICK
        );

        int triggeredTraps = 0;

        for (NodeId target : action.getTargetNodes()) {
            NodeId previous = character.getCurrentNode();
            character.setCurrentNode(target);
            logService.action(
                    game,
                    playerSide,
                    "You moved in node " + target
            );
            triggerWire(
                    game,
                    playerSide,
                    previous,
                    target
            );
            MapEffect beartrap =
                    hasBeartrap(
                            game,
                            playerSide,
                            target
                    );
            if (beartrap == null) {
                continue;
            }
            triggeredTraps++;
            boolean shouldTrap =
                    !stick || triggeredTraps >= 2;
            if (shouldTrap) {
                statuses.add(
                        new StatusEffect(
                                StatusEffectType.TRAPPED,
                                1
                        )
                );
                logService.itemTriggered(
                        game,
                        playerSide,
                        "You stepped into a beartrap, can't do much while being trapped..."
                );
                return;
            }
            removeBeartrap(
                    game,
                    playerSide,
                    target
            );
        }
        removeStatus(
                statuses,
                StatusEffectType.STICK
        );
    }

    private GameStateResponse resolveNapalmSecondHit(
            Game game,
            boolean playerSide
    ) {

        List<MapEffect> effects =
                game.getMapEffects()
                        .stream()
                        .filter(effect ->
                                effect.getType()
                                        == MapEffectType.NAPALM_SECOND_HIT
                                        && effect.isBelongsToPlayer()
                                        == playerSide
                        )
                        .toList();

        for (MapEffect effect : effects) {

            GameStateResponse response = strikeNode(
                    game,
                    playerSide,
                    effect.getFirstNode()
            );

            logService.itemTriggered(
                    game,
                    playerSide,
                    "The napalm burns for one more turn in " + effect.getFirstNode() + " but doesn't hit anything"
            );

            if (response != null) {
                return response;
            }
        }

        game.getMapEffects().removeIf(effect ->
                effect.getType()

                        == MapEffectType.NAPALM_SECOND_HIT
                        && effect.isBelongsToPlayer()
                        == playerSide
        );

        return null;
    }

    private GameStateResponse resolveStrike(
            Game game,
            TurnRequest action,
            boolean playerSide
    ) {
        NodeId target = action.getTargetNodes().get(0);
        List<StatusEffect> statuses = getStatuses(game, playerSide);

        boolean silenced = hasStatus(statuses, StatusEffectType.SILENCER);

        //NORMAL HIT
        if(!silenced){
            logService.action(game, playerSide, "You strike at node " + target);
        }else{
            logService.action(game, playerSide, "You strike at node " + target + ", your silencer won't reveal your position");
        }
        GameStateResponse response = strikeNode (game, playerSide, target);
        if (response != null) {
            return response;
        }

        //NUKE
        if (hasStatus(statuses, StatusEffectType.NUKE_READY)) {
            logService.action(
                    game,
                    playerSide,
                    "And your nuke also hits the neighbouring nodes"
            );
            for (NodeId neighbour : MapDefinition.getNeighbours(target)) {
                if (MapDefinition.isTunnel(target, neighbour)) {
                    continue;
                }
                response = strikeNode(game, playerSide, neighbour);
                if (response != null) {
                    return response;
                }
            }
            removeStatus(statuses, StatusEffectType.NUKE_READY);
        }

        //NAPALM FIRST HIT
        if (hasStatus(statuses, StatusEffectType.NAPALM_READY)) {
            logService.action(
                    game,
                    playerSide,
                    "Napalm will burn on " + target + " for the following turn"
            );
            game.getMapEffects().add(
                    new MapEffect(
                            MapEffectType.NAPALM_SECOND_HIT,
                            target,
                            null,
                            playerSide
                    )
            );
            removeStatus(statuses, StatusEffectType.NAPALM_READY);
        }


        if (!silenced) {
            addSmokeWarning(game, playerSide, target);
            logService.action(
                    game,
                    playerSide,
                    "You don't hit anything, be careful, your location has been revealed!"
            );
        }

        return null;
    }

    private GameStateResponse applyDamage(
            Game game,
            boolean playerSide
    ) {
        CharacterState damagedCharacter =
                getCharacter(game, !playerSide);

        if (!(playerSide
                && game.getMode() == GameMode.ENDLESS)) {

            damagedCharacter.setHealth(
                    damagedCharacter.getHealth() - 1
            );
        }
        else {

            int newScore =
                    game.getEndlessScore() + 1;

            game.setEndlessScore(newScore);

            User user = game.getUser();

            if (newScore > user.getBestEndlessScore()) {
                user.setBestEndlessScore(newScore);
            }
        }

        game.getBossInventory().clear();
        game.getPlayerInventory().clear();
        game.getPlayerStatusEffects().clear();
        game.getBossStatusEffects().clear();
        game.getMapEffects().clear();
        game.getLogs().clear();
        game.setTurnNumber(1);

        game.getPlayer()
                .setCurrentNode(
                        MapDefinition.getRandomNode()
                );

        game.getBoss()
                .setCurrentNode(
                        MapDefinition.getRandomNode()
                );

        logService.system(
                game,
                playerSide,
                "You hit the enemy! The battlefield has been reset."
        );

        logService.system(
                game,
                !playerSide,
                "You've been hit! The battlefield has been reset."
        );

        boolean playerDead = game.getPlayer().getHealth() <= 0;

        if(playerDead) {
            return applyVictory(game, GameResult.DEFEAT);
        }

        boolean bossDead = game.getBoss().getHealth() <= 0;

        if(bossDead){
            return applyVictory(game, GameResult.VICTORY);
        }

        gameRepository.save(game);

        GameStateResponse response =
                GameMapper.toStateResponse(game);

        response.setGameOver(false);
        response.setResult(null);

        return response;
    }

    private GameStateResponse applyVictory(
            Game game,
            GameResult result
    ) {
        GameStateResponse response =
                GameMapper.toStateResponse(game);

        response.setGameOver(true);
        response.setResult(result);

        gameRepository.delete(game);

        return response;
    }

    private void resolveSleep(
            Game game,
            boolean playerSide
    ) {
        CharacterState character = getCharacter(game, playerSide);

        NodeId currentNode = character.getCurrentNode();

        logService.action(
                game,
                playerSide,
                "You choose to sleep at node " + currentNode + " for the night"
        );

        resolveSingleSleepEffect(
                game,
                playerSide
        );

        if (hasMapEffect(
                game,
                MapEffectType.CAMPFIRE,
                playerSide,
                currentNode
        )) {

            logService.itemTriggered(
                    game,
                    playerSide,
                    "The campfire was lit and the effects of sleep were doubled."
            );

            addSmokeWarning(game, playerSide, currentNode);

            resolveSingleSleepEffect(
                    game,
                    playerSide
            );
        }
    }

    private void resolvePersistentEffects(
            Game game,
            boolean playerSide,
            TurnRequest request
    ) {
        final int NAPALM_CHARGE_TIME = 2;
        final int NUKE_CHARGE_TIME = 3;

        List<StatusEffect> statuses =
                getStatuses(game, playerSide);

        // STICK
        removeStatus(
                statuses,
                StatusEffectType.STICK
        );

        // SILENCER
        removeStatus(
                statuses,
                StatusEffectType.SILENCER
        );

        // CAMPFIRE
        game.getMapEffects().removeIf(effect ->
                effect.isBelongsToPlayer() == playerSide
                        && effect.getType() == MapEffectType.CAMPFIRE
        );

        // UMBRELLA
        game.getMapEffects().removeIf(effect ->
                effect.isBelongsToPlayer() == playerSide
                        && effect.getType() == MapEffectType.UMBRELLA
        );

        // charging reset
        if ((request.getActionType() != ActionType.SLEEP)) {

            for (StatusEffect effect : statuses) {

                if (effect.getType() != StatusEffectType.NUKE_CHARGING) {
                    continue;
                }

                if (effect.getRemainingTurns() < NUKE_CHARGE_TIME) {
                    resetCharging(
                            game,
                            playerSide,
                            StatusEffectType.NUKE_CHARGING,
                            NUKE_CHARGE_TIME
                    );
                }
            }

            for (StatusEffect effect : statuses) {

                if (effect.getType() != StatusEffectType.NAPALM_CHARGING) {
                    continue;
                }

                if (effect.getRemainingTurns() < NAPALM_CHARGE_TIME) {
                    resetCharging(
                            game,
                            playerSide,
                            StatusEffectType.NAPALM_CHARGING,
                            NAPALM_CHARGE_TIME
                    );
                }
            }
        }
    }

    private void resetCharging(
            Game game,
            boolean playerSide,
            StatusEffectType type,
            int turns
    ) {

        for (StatusEffect effect :
                getStatuses(game, playerSide)) {

            if (effect.getType() != type) {
                continue;
            }
            
            String name = "";
            if (type==StatusEffectType.NUKE_CHARGING){name="Nuke";}
            if (type==StatusEffectType.NAPALM_CHARGING){name="Napalm";}

            effect.setRemainingTurns(turns);

            logService.itemTriggered(
                    game,
                    playerSide,
                    name + " charging was interrupted."
            );

            return;
        }
    }

    //GENERIC HELPERS
    private boolean hasStatus(
            List<StatusEffect> effects,
            StatusEffectType type
    ) {
        return effects.stream()
                .anyMatch(effect ->
                        effect.getType() == type
                );
    }

    private boolean hasMapEffect(
            Game game,
            MapEffectType type,
            boolean playerSide,
            NodeId node
    ) {
        return game.getMapEffects()
                .stream()
                .anyMatch(effect ->
                        effect.getType() == type
                                && effect.isBelongsToPlayer() == playerSide
                                && effect.getFirstNode() == node
                );
    }

    private CharacterState getCharacter(
            Game game,
            boolean playerSide
    ) {
        return playerSide
                ? game.getPlayer()
                : game.getBoss();
    }

    private List<StatusEffect> getStatuses(
            Game game,
            boolean playerSide
    ) {
        return playerSide
                ? game.getPlayerStatusEffects()
                : game.getBossStatusEffects();
    }

    private void removeStatus(
            List<StatusEffect> effects,
            StatusEffectType type
    ) {
        effects.removeIf(effect ->
                effect.getType() == type
        );
    }

    //Helper Beartrap
    private MapEffect hasBeartrap(
            Game game,
            boolean playerSide,
            NodeId node
    ) {
        return game.getMapEffects()
                .stream()
                .filter(effect ->
                        effect.getType() == MapEffectType.BEARTRAP
                                && effect.isBelongsToPlayer() != playerSide
                                && effect.getFirstNode() == node
                )
                .findFirst()
                .orElse(null);
    }

    private void removeBeartrap(
            Game game,
            boolean playerSide,
            NodeId node
    ) {
        game.getMapEffects().removeIf(effect ->
                effect.getType() == MapEffectType.BEARTRAP
                        && effect.isBelongsToPlayer() != playerSide
                        && effect.getFirstNode() == node
        );
        logService.itemTriggered(
                game,
                !playerSide,
                ("A trap mysteriously disappeared from node " + node)
        );
    }

    //Helper Wire
    private void triggerWire(
            Game game,
            boolean playerSide,
            NodeId from,
            NodeId to
    ) {
        boolean detected =
                game.getMapEffects()
                        .stream()
                        .anyMatch(effect ->
                                effect.getType() == MapEffectType.WIRE
                                        && effect.isBelongsToPlayer() != playerSide
                                        && (
                                        (effect.getFirstNode() == from
                                                && effect.getSecondNode() == to)
                                                ||
                                                (effect.getFirstNode() == to
                                                        && effect.getSecondNode() == from)
                                )
                        );
        if (!detected) {
            return;
        }
        logService.itemTriggered(
                game,
                !playerSide,
                "A movement has been detected between "
                        + from
                        + " and "
                        + to
        );
    }

    //Strike Helper
    private GameStateResponse strikeNode(
            Game game,
            boolean playerSide,
            NodeId target
    ) {

        CharacterState enemy =
                getCharacter(game, !playerSide);

        if (enemy.getCurrentNode() != target) {
            return null;
        }

        boolean protectedByUmbrella =
                hasMapEffect(
                        game,
                        MapEffectType.UMBRELLA,
                        !playerSide,
                        target
                );

        if (protectedByUmbrella) {

            logService.itemTriggered(
                    game,
                    !playerSide,
                    "Your umbrella protected you from an enemy strike."
            );

            return null;
        }

        return applyDamage(game, playerSide);
    }

    private void addSmokeWarning(
            Game game,
            boolean playerSide,
            NodeId node
    ) {
        logService.system(
                game,
                !playerSide,
                "You see smoke coming from " + node
        );
    }

    //HELPER SLEEP
    private boolean progressCharging(
            Game game,
            boolean playerSide,
            StatusEffectType charging,
            StatusEffectType ready
    ) {

        List<StatusEffect> statuses =
                getStatuses(game, playerSide);

        for (StatusEffect effect : statuses) {

            if (effect.getType() != charging) {
                continue;
            }

            effect.setRemainingTurns(
                    effect.getRemainingTurns() - 1
            );

            if (effect.getRemainingTurns() > 0) {

                String name = "";
                if (charging==StatusEffectType.NUKE_CHARGING){name="Nuke";}
                if (charging==StatusEffectType.NAPALM_CHARGING){name="Napalm";}

                logService.itemTriggered(
                        game,
                        playerSide,
                        name + " charging: "
                                + effect.getRemainingTurns()
                                + " turn(s) remaining."
                );
                return true;
            }

            removeStatus(
                    statuses,
                    charging
            );

            statuses.add(
                    new StatusEffect(
                            ready,
                            1
                    )
            );

            String name = "";
            if (ready==StatusEffectType.NUKE_READY){name="Nuke";}
            if (ready==StatusEffectType.NAPALM_READY){name="Napalm";}


            logService.itemTriggered(
                    game,
                    playerSide,
                    name + " is now ready."
            );

            return true;
        }

        return false;
    }

    private void resolveSingleSleepEffect(
            Game game,
            boolean playerSide
    ) {

        if (progressCharging(
                game,
                playerSide,
                StatusEffectType.NUKE_CHARGING,
                StatusEffectType.NUKE_READY
        )) {
            return;
        }

        if (progressCharging(
                game,
                playerSide,
                StatusEffectType.NAPALM_CHARGING,
                StatusEffectType.NAPALM_READY
        )) {
            return;
        }

        giveRandomItem(
                game,
                playerSide
        );
    }

    private void giveRandomItem(
            Game game,
            boolean playerSide
    ) {
        final int MAX_INVENTORY_NUMBER= 8;

        List<ItemType> inventory =
                playerSide
                        ? game.getPlayerInventory()
                        : game.getBossInventory();

        if (inventory.size() >= MAX_INVENTORY_NUMBER) {

            logService.itemFound(
                    game,
                    playerSide,
                    "Your mind is too full, you cannot get any more items, use them or forget them."
            );

            return;
        }

        ItemType[] items = ItemType.values();

        final Random RANDOM = new Random();

        ItemType randomItem =
                items[RANDOM.nextInt(items.length)];

        inventory.add(randomItem);

        logService.itemFound(
                game,
                playerSide,
                "You conjured a " + randomItem.getDisplayName() + " in your sleep"
        );
    }
}

