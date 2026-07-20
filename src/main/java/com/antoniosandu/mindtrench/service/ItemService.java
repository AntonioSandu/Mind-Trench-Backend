package com.antoniosandu.mindtrench.service;

import com.antoniosandu.mindtrench.dto.request.UseItemRequest;
import com.antoniosandu.mindtrench.dto.response.GameStateResponse;
import com.antoniosandu.mindtrench.entity.*;
import com.antoniosandu.mindtrench.entity.enums.ItemType;
import com.antoniosandu.mindtrench.entity.enums.MapEffectType;
import com.antoniosandu.mindtrench.entity.enums.MapLayer;
import com.antoniosandu.mindtrench.entity.enums.StatusEffectType;
import com.antoniosandu.mindtrench.exception.GameNotFoundException;
import com.antoniosandu.mindtrench.exception.InvalidItemException;
import com.antoniosandu.mindtrench.game.map.MapDefinition;
import com.antoniosandu.mindtrench.game.map.NodeId;
import com.antoniosandu.mindtrench.mapper.GameMapper;
import com.antoniosandu.mindtrench.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class ItemService {

    private final LogService logService;
    private final GameRepository gameRepository;

    public ItemService(
            LogService logService,
            GameRepository gameRepository
    ) {
        this.logService = logService;
        this.gameRepository = gameRepository;
    }

    public GameStateResponse forgetItem(
            Long gameId,
            Long userId,
            UseItemRequest request
    ){
        boolean playerSide = true;
        Game game = gameRepository
                .findByIdAndUserId(gameId, userId)
                .orElseThrow(() ->
                        new GameNotFoundException(
                                "Game not found"));

        if (hasStatus(game.getPlayerStatusEffects(),
                StatusEffectType.TRAPPED)) {
            throw new InvalidItemException(
                    "You are trapped and cannot forget items."
            );
        }

        List<ItemType> inventory = game.getPlayerInventory();
        int index = request.getInventoryIndex();

        if (index < 0 || index >= inventory.size()) {
            throw new InvalidItemException(
                    "Invalid inventory index"
            );
        }
        ItemType item = inventory.get(index);
        String name = item.getDisplayName();

        inventory.remove(index);

        logService.itemUsed(
                game,
                playerSide,
                "You cleared your mind and forgot the " + name + "."
        );

        gameRepository.save(game);

        GameStateResponse response=GameMapper.toStateResponse(game);
        response.setResult(null);
        response.setGameOver(false);
        return response;
    }

    public GameStateResponse useItem(
            Long gameId,
            Long userId,
            UseItemRequest request
    ) {
        Game game = gameRepository
                .findByIdAndUserId(gameId, userId)
                .orElseThrow(() ->
                        new GameNotFoundException(
                                "Game not found"));

        if (hasStatus(game.getPlayerStatusEffects(),
                StatusEffectType.TRAPPED)) {
            throw new InvalidItemException(
                    "You are trapped and cannot use items."
            );
        }

        List<ItemType> inventory = game.getPlayerInventory();
        int index = request.getInventoryIndex();

        if (index < 0 || index >= inventory.size()) {
            throw new InvalidItemException(
                    "Invalid inventory index"
            );
        }

        ItemType item = inventory.get(index);

        useItem(
                game,
                true,
                item,
                request
        );

        inventory.remove(index);

        gameRepository.save(game);
        GameStateResponse response=GameMapper.toStateResponse(game);
        response.setResult(null);
        response.setGameOver(false);
        return response;
    }

    public void useItem(
            Game game,
            boolean playerSide,
            ItemType item,
            UseItemRequest request
    ) {

        switch (item) {
            case REVELATION -> useRevelation(game, playerSide);
            case BIRD -> useBird(game, playerSide);
            case UMBRELLA -> useUmbrella(game, playerSide);
            case CAMPFIRE -> useCampfire(game, playerSide);
            case STICK -> useStick(game, playerSide);
            case WIRE -> useWire(game, playerSide, request);
            case BEARTRAP -> useBeartrap(game, playerSide, request);
            case SHEARS -> useShears(game, playerSide);
            case SILENCER -> useSilencer(game, playerSide);
            case NUKE -> useNuke(game, playerSide);
            case NAPALM -> useNapalm(game, playerSide);

            default -> throw new UnsupportedOperationException(
                    "Item not implemented yet: " + item
            );
        }
    }

    private void useUmbrella(
            Game game,
            boolean playerSide
    ) {

        NodeId currentNode =
                playerSide
                        ? game.getPlayer().getCurrentNode()
                        : game.getBoss().getCurrentNode();

        if (isNodeOccupied(
                game,
                currentNode,
                playerSide,
                MapLayer.OWN_NODE
        )) {

            throw new InvalidItemException(
                    "There is already an object here."
            );
        }

        game.getMapEffects().add(
                new MapEffect(
                        MapEffectType.UMBRELLA,
                        currentNode,
                        null,
                        playerSide
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You opened an umbrella on node "
                        + currentNode
        );
    }

    private void useRevelation(
            Game game,
            boolean playerSide
    ) {
        List<ItemType> enemyInventory =
                playerSide
                        ? game.getBossInventory()
                        : game.getPlayerInventory();

        String items = enemyInventory.stream()
                .map(ItemType::getDisplayName)
                .toList()
                .toString();

        logService.itemUsed(
                game,
                playerSide,
                "The revelation tells you the enemy's unused items: " + items
        );
    }

    private void useStick(
            Game game,
            boolean playerSide
    ){
        List<StatusEffect> effects =
                playerSide
                        ? game.getPlayerStatusEffects()
                        : game.getBossStatusEffects();

        effects.add(
                new StatusEffect(
                        StatusEffectType.STICK,
                        1
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You used a stick."
        );
    }

    private void useCampfire(
            Game game,
            boolean playerSide
    ) {

        NodeId currentNode =
                playerSide
                        ? game.getPlayer().getCurrentNode()
                        : game.getBoss().getCurrentNode();

        if (isNodeOccupied(
                game,
                currentNode,
                playerSide,
                MapLayer.OWN_NODE
        )) {

            throw new InvalidItemException(
                    "There is already an object here."
            );
        }

        game.getMapEffects().add(
                new MapEffect(
                        MapEffectType.CAMPFIRE,
                        currentNode,
                        null,
                        playerSide
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You put up a campfire on node "
                        + currentNode
        );
    }

    private void useBird(
            Game game,
            boolean playerSide
    ) {

        CharacterState enemy =
                playerSide
                        ? game.getBoss()
                        : game.getPlayer();

        NodeId enemyNode = enemy.getCurrentNode();

        List<NodeId> allNodes = new ArrayList<>(List.of(NodeId.values()));

        allNodes.remove(enemyNode);

        Collections.shuffle(allNodes);

        List<NodeId> fakeNodes = allNodes.subList(0, 2);

        List<NodeId> result = new ArrayList<>();
        result.add(enemyNode);
        result.addAll(fakeNodes);

        Collections.shuffle(result); // per non dare pattern

        logService.itemUsed(
                game,
                playerSide,
                "The bird whispers: " + result
        );
    }

    private void useWire(
            Game game,
            boolean playerSide,
            UseItemRequest request
    ) {
        NodeId first = request.getFirstNode();
        NodeId second = request.getSecondNode();

        if (first == null || second == null) {
            throw new InvalidItemException(
                    "Wire requires two nodes."
            );
        }

        if (!MapDefinition.areConnected(first, second)) {
            throw new InvalidItemException(
                    "Nodes are not connected."
            );
        }

        if (MapDefinition.isTunnel(first, second)) {
            throw new InvalidItemException(
                    "You cannot place a wire inside a tunnel."
            );
        }

        if (isEdgeOccupied(
                game,
                first,
                second,
                playerSide
        )) {
            throw new InvalidItemException(
                    "There is already a wire here."
            );
        }

        game.getMapEffects().add(
                new MapEffect(
                        MapEffectType.WIRE,
                        first,
                        second,
                        playerSide
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You placed a wire between "
                        + first + " and " + second
        );
    }

    private void useBeartrap(
            Game game,
            boolean playerSide,
            UseItemRequest request
    ) {
        NodeId node = request.getFirstNode();

        if (node == null) {
            throw new InvalidItemException(
                    "Beartrap requires a node."
            );
        }

        if (isNodeOccupied(
                game,
                node,
                playerSide,
                MapLayer.ENEMY_NODE
        )) {
            throw new InvalidItemException(
                    "There is already an object here."
            );
        }

        game.getMapEffects().add(
                new MapEffect(
                        MapEffectType.BEARTRAP,
                        node,
                        null,
                        playerSide
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You placed a beartrap on node "
                        + node
        );
    }

    private boolean canReachWithShears(
            NodeId currentNode,
            MapEffect effect
    ) {

        if (effect.getType() == MapEffectType.BEARTRAP) {
            return effect.getFirstNode() == currentNode
                    || MapDefinition.areConnected(
                    currentNode,
                    effect.getFirstNode()
            );
        }

        if (effect.getType() == MapEffectType.WIRE) {

            if (MapDefinition.isTunnel(
                    effect.getFirstNode(),
                    effect.getSecondNode()
            )) {
                return false;
            }

            return MapDefinition.areConnected(
                    currentNode,
                    effect.getFirstNode()
            ) || MapDefinition.areConnected(
                    currentNode,
                    effect.getSecondNode()
            );
        }

        return false;
    }

    private void useShears(
            Game game,
            boolean playerSide
    ) {
        NodeId currentNode =
                playerSide
                        ? game.getPlayer().getCurrentNode()
                        : game.getBoss().getCurrentNode();

        List<MapEffect> removableEffects =
                game.getMapEffects()
                        .stream()
                        .filter(effect ->
                                effect.isBelongsToPlayer() != playerSide
                        )
                        .filter(effect ->
                                effect.getType() == MapEffectType.WIRE
                                        ||
                                        effect.getType() == MapEffectType.BEARTRAP
                        )
                        .filter(effect ->
                                canReachWithShears(
                                        currentNode,
                                        effect
                                )
                        )
                        .toList();

        if (removableEffects.isEmpty()) {
            logService.itemUsed(
                    game,
                    playerSide,
                    "The shears found nothing to cut."
            );
            return;
        }

        Random random = new Random();

        MapEffect removed =
                removableEffects.get(
                        random.nextInt(removableEffects.size())
                );

        game.getMapEffects().remove(removed);

        logService.itemUsed(
                game,
                playerSide,
                "You removed an enemy "
                        + removed.getType()
                        .name()
                        .toLowerCase()
        );
    }

    private void useSilencer(
            Game game,
            boolean playerSide
    ) {
        List<StatusEffect> effects =
                playerSide
                        ? game.getPlayerStatusEffects()
                        : game.getBossStatusEffects();

        effects.add(
                new StatusEffect(
                        StatusEffectType.SILENCER,
                        1
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You equipped a silencer."
        );
    }

    private void useNuke(
            Game game,
            boolean playerSide
    ) {
        final int NUKE_CHARGE_TIME = 3;

        startCharging(
                game,
                playerSide,
                StatusEffectType.NUKE_CHARGING,
                NUKE_CHARGE_TIME,
                "Nuke"
        );
    }

    private void useNapalm(
            Game game,
            boolean playerSide
    ){
        final int NAPALM_CHARGE_TIME = 2;

        startCharging(
                game,
                playerSide,
                StatusEffectType.NAPALM_CHARGING,
                NAPALM_CHARGE_TIME,
                "Napalm"
        );
    }

    //HELPERS
    private boolean hasStatus(
            List<StatusEffect> effects,
            StatusEffectType type
    ) {
        return effects.stream()
                .anyMatch(effect ->
                        effect.getType() == type
                );
    }

    private boolean isNodeOccupied(
            Game game,
            NodeId node,
            boolean playerSide,
            MapLayer layer
    ) {

        return game.getMapEffects()
                .stream()
                .anyMatch(effect ->

                        effect.getFirstNode() == node
                                && effect.isBelongsToPlayer() == playerSide
                                && effect.getType().getLayer() == layer
                );
    }

    private boolean isEdgeOccupied(
            Game game,
            NodeId a,
            NodeId b,
            boolean playerSide
    ) {

        return game.getMapEffects()
                .stream()
                .anyMatch(effect ->

                        effect.getType() == MapEffectType.WIRE
                                && effect.isBelongsToPlayer() == playerSide
                                && (

                                (effect.getFirstNode() == a
                                        && effect.getSecondNode() == b)

                                        ||

                                        (effect.getFirstNode() == b
                                                && effect.getSecondNode() == a)
                        )
                );
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

    private boolean hasStrikeModifier(
            List<StatusEffect> statuses
    ) {
        return hasStatus(statuses, StatusEffectType.NUKE_CHARGING)
                || hasStatus(statuses, StatusEffectType.NUKE_READY)
                || hasStatus(statuses, StatusEffectType.NAPALM_CHARGING)
                || hasStatus(statuses, StatusEffectType.NAPALM_READY);
    }

    private void startCharging(
            Game game,
            boolean playerSide,
            StatusEffectType chargingType,
            int turns,
            String itemName
    ) {
        List<StatusEffect> statuses = getStatuses(game, playerSide);

        if (hasStrikeModifier(statuses)) {
            throw new InvalidItemException(
                    "A strike modifier is already charging or ready."
            );
        }

        statuses.add(
                new StatusEffect(
                        chargingType,
                        turns
                )
        );

        logService.itemUsed(
                game,
                playerSide,
                "You started charging " + itemName + "."
        );
    }
}
