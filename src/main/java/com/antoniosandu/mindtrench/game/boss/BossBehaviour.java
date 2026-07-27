package com.antoniosandu.mindtrench.game.boss;

import com.antoniosandu.mindtrench.dto.request.TurnRequest;
import com.antoniosandu.mindtrench.dto.request.UseItemRequest;
import com.antoniosandu.mindtrench.entity.Game;
import com.antoniosandu.mindtrench.entity.LogEntry;
import com.antoniosandu.mindtrench.entity.MapEffect;
import com.antoniosandu.mindtrench.entity.StatusEffect;
import com.antoniosandu.mindtrench.entity.enums.ItemType;
import com.antoniosandu.mindtrench.entity.enums.MapEffectType;
import com.antoniosandu.mindtrench.entity.enums.MapLayer;
import com.antoniosandu.mindtrench.entity.enums.StatusEffectType;
import com.antoniosandu.mindtrench.exception.InvalidItemException;
import com.antoniosandu.mindtrench.game.action.ActionType;
import com.antoniosandu.mindtrench.game.map.MapDefinition;
import com.antoniosandu.mindtrench.game.map.NodeId;
import com.antoniosandu.mindtrench.service.ItemService;

import java.util.*;
import java.util.function.ToDoubleFunction;

public final class BossBehaviour {

    private BossBehaviour() {}

    private static final Random RNG = new Random();

    // ----- ACTION WEIGHTS -----
    private static final double BASE_MOVE_WEIGHT = 25;
    private static final double BASE_SLEEP_WEIGHT = 50;
    private static final double BASE_STRIKE_WEIGHT = 25;

    private static final double AFTER_STRIKE_MOVE_BONUS = 25;
    private static final double AFTER_STRIKE_STRIKE_PENALTY = 5;
    private static final double SMOKE_STRIKE_BONUS = 35;

    private static final double BOSS_SELF_REVEALED_MOVE_BONUS = 20;
    private static final double JUST_STAYED_STILL_MOVE_BONUS = 20;
    private static final double WIRE_DETECTED_STRIKE_BONUS = 25;
    private static final double BEARTRAP_GONE_STRIKE_BONUS = 25;
    private static final double BIRD_USED_STRIKE_BONUS = 20;
    private static final double REVELATION_ENEMY_BIRD_MOVE_BONUS = 15;

    private static final double CHARGING_SLEEP_BONUS = 150;
    private static final double CHARGING_MOVE_PENALTY_FACTOR = 0.5;
    private static final double CHARGING_STRIKE_PENALTY_FACTOR = 0.5;
    private static final double READY_STRIKE_BONUS = 50;

    // ----- MOVEMENT WEIGHTS -----
    private static final double BACKTRACK_PENALTY_FACTOR = 0.8;
    private static final double SENSED_BEARTRAP_DANGER_FACTOR = 0.2;
    private static final double SENSED_WIRE_DANGER_FACTOR = 0.4;

    // ----- NODE HINT WEIGHTS (used for strike targeting AND item placement) -----
    private static final double SMOKE_TARGET_WEIGHT = 4;
    private static final double SMOKE_NEIGHBOUR_WEIGHT = 3;
    private static final double WIRE_DETECTED_NODE_WEIGHT = 3;
    private static final double WIRE_DETECTED_NEIGHBOUR_WEIGHT = 1.5;
    private static final double BEARTRAP_GONE_NODE_WEIGHT = 3;
    private static final double BEARTRAP_GONE_NEIGHBOUR_WEIGHT = 1.5;
    private static final double BIRD_CANDIDATE_NODE_WEIGHT = 2.5;
    private static final double BIRD_CANDIDATE_NEIGHBOUR_WEIGHT = 1;
    private static final double DEFAULT_NODE_WEIGHT = 1;

    // ----- ITEM USAGE -----
    private static final int MAX_INVENTORY = 8;
    private static final double ITEM_USE_INITIAL_CHANCE = 0.5;
    private static final double ITEM_USE_DECAY = 0.15;

    private static final Set<ItemType> ONCE_ONLY_TYPES = EnumSet.of(
            ItemType.REVELATION, ItemType.BIRD, ItemType.SHEARS,
            ItemType.STICK, ItemType.SILENCER,
            ItemType.UMBRELLA, ItemType.CAMPFIRE,
            ItemType.NUKE, ItemType.NAPALM
    );

    public static TurnRequest generateAction(Game game, ItemService itemService) {

        List<StatusEffect> bossStatuses = game.getBossStatusEffects();

        if (hasStatus(bossStatuses, StatusEffectType.TRAPPED)) {
            // if boss is trapped: no items, only STAY_STILL
            return new TurnRequest(ActionType.STAY_STILL, List.of());
        }

        TurnSignals signals = collectTurnSignals(game);

        BossContext ctx = new BossContext();
        useItemsPhase(game, itemService, ctx, signals);

        // statuses / map effects may have changed after using items, re-read them
        bossStatuses = game.getBossStatusEffects();
        NodeId bossNode = game.getBoss().getCurrentNode();

        boolean hasStick = hasStatus(bossStatuses, StatusEffectType.STICK);
        boolean hasSilencer = hasStatus(bossStatuses, StatusEffectType.SILENCER);
        boolean hasCampfire = hasMapEffect(game, MapEffectType.CAMPFIRE, false, bossNode);
        boolean hasUmbrella = hasMapEffect(game, MapEffectType.UMBRELLA, false, bossNode);

        ActionType chosen;

        if (hasCampfire) {
            // campfire only pays off if we sleep this same turn
            chosen = ActionType.SLEEP;
        } else if (hasStick) {
            // stick only pays off if we move this same turn
            chosen = ActionType.MOVE;
        } else if (hasSilencer) {
            // silencer only pays off if we strike this same turn
            chosen = ActionType.STRIKE;
        } else if (hasUmbrella) {
            // umbrella protects the current node: staying to sleep or striking
            // both make sense, moving away wastes the shield
            Map<ActionType, Double> binary = new LinkedHashMap<>();
            binary.put(ActionType.SLEEP, 1.0);
            binary.put(ActionType.STRIKE, 1.0);
            chosen = weightedPick(binary);
        } else {
            chosen = chooseAction(
                    signals,
                    !ctx.birdCandidates.isEmpty(),
                    ctx.enemyHasBird,
                    bossStatuses
            );
        }

        return switch (chosen) {
            case MOVE -> buildMove(game, hasStick, ctx.dangerNodes, ctx.dangerEdges);
            case SLEEP -> new TurnRequest(ActionType.SLEEP, List.of());
            case STRIKE -> buildStrike(computeNodeHints(signals, ctx));
            default -> new TurnRequest(ActionType.SLEEP, List.of());
        };
    }

    private static ActionType chooseAction(
            TurnSignals s,
            boolean birdUsedThisTurn,
            boolean enemyHasBirdRevealed,
            List<StatusEffect> bossStatuses
    ) {

        boolean ready = hasStatus(bossStatuses, StatusEffectType.NUKE_READY)
                || hasStatus(bossStatuses, StatusEffectType.NAPALM_READY);

        double moveWeight = BASE_MOVE_WEIGHT;
        double sleepWeight = BASE_SLEEP_WEIGHT;
        double strikeWeight = BASE_STRIKE_WEIGHT;

        if (s.justStruck()) {
            moveWeight += AFTER_STRIKE_MOVE_BONUS;
            strikeWeight = Math.max(2, strikeWeight - AFTER_STRIKE_STRIKE_PENALTY);
        }

        if (s.knowsPlayerPosition()) {
            strikeWeight += SMOKE_STRIKE_BONUS;
        }

        if (s.bossRevealedSelfLastTurn()) {
            moveWeight += BOSS_SELF_REVEALED_MOVE_BONUS;
        }

        if (s.justStayedStillLastTurn()) {
            moveWeight += JUST_STAYED_STILL_MOVE_BONUS;
        }

        if (s.wireDetectedLastTurn()) {
            strikeWeight += WIRE_DETECTED_STRIKE_BONUS;
        }

        if (s.beartrapGoneLastTurn()) {
            strikeWeight += BEARTRAP_GONE_STRIKE_BONUS;
        }

        if (birdUsedThisTurn) {
            strikeWeight += BIRD_USED_STRIKE_BONUS;
        }

        if (enemyHasBirdRevealed) {
            moveWeight += REVELATION_ENEMY_BIRD_MOVE_BONUS;
        }

        if (s.sleptToChargeLastTurn() && !ready) {
            // if it stated charging a strike modifier, it should keep charging until it is ready
            sleepWeight += CHARGING_SLEEP_BONUS;
            moveWeight *= CHARGING_MOVE_PENALTY_FACTOR;
            strikeWeight *= CHARGING_STRIKE_PENALTY_FACTOR;
        }

        if (ready) {
            // charged strike modifier ready to use, better fire it off
            strikeWeight += READY_STRIKE_BONUS;
        }

        Map<ActionType, Double> weights = new LinkedHashMap<>();
        weights.put(ActionType.MOVE, moveWeight);
        weights.put(ActionType.SLEEP, sleepWeight);
        weights.put(ActionType.STRIKE, strikeWeight);

        return weightedPick(weights);
    }

    // ---------------- MOVE ----------------

    private static TurnRequest buildMove(
            Game game,
            boolean hasStick,
            Set<NodeId> dangerNodes,
            Set<EdgeKey> dangerEdges
    ) {

        NodeId current = game.getBoss().getCurrentNode();
        NodeId cameFrom = findPreviousBossNode(game);

        NodeId firstHop = pickNeighbour(current, cameFrom, dangerNodes, dangerEdges);
        List<NodeId> path = new ArrayList<>();
        path.add(firstHop);

        if (hasStick) {
            NodeId secondHop = pickNeighbour(firstHop, current, dangerNodes, dangerEdges);
            path.add(secondHop);
        }

        return new TurnRequest(ActionType.MOVE, path);
    }

    private static NodeId pickNeighbour(
            NodeId from,
            NodeId avoid,
            Set<NodeId> dangerNodes,
            Set<EdgeKey> dangerEdges
    ) {

        Set<NodeId> neighbours = MapDefinition.getNeighbours(from);

        Map<NodeId, Double> weights = new LinkedHashMap<>();
        for (NodeId n : neighbours) {
            double w = DEFAULT_NODE_WEIGHT;

            if (n == avoid) {
                w *= BACKTRACK_PENALTY_FACTOR;
            }

            // a sensed, still-active enemy beartrap is worse than a sensed wire:
            // stepping on it traps the boss, crossing a wire only reveals our movement
            if (dangerNodes.contains(n)) {
                w *= SENSED_BEARTRAP_DANGER_FACTOR;
            } else if (dangerEdges.contains(EdgeKey.of(from, n))) {
                w *= SENSED_WIRE_DANGER_FACTOR;
            }

            weights.put(n, w);
        }

        return weightedPick(weights);
    }

    // ---------------- STRIKE ----------------

    private static TurnRequest buildStrike(Map<NodeId, Double> hints) {

        Map<NodeId, Double> weights = new LinkedHashMap<>();
        for (NodeId node : NodeId.values()) {
            weights.put(node, DEFAULT_NODE_WEIGHT + hints.getOrDefault(node, 0.0));
        }

        NodeId target = weightedPick(weights);

        return new TurnRequest(ActionType.STRIKE, List.of(target));
    }

    private static Map<NodeId, Double> computeNodeHints(TurnSignals s, BossContext ctx) {

        Map<NodeId, Double> hints = new EnumMap<>(NodeId.class);

        if (s.smokeNode() != null) {
            addHint(hints, s.smokeNode(), SMOKE_TARGET_WEIGHT);
            for (NodeId n : MapDefinition.getNeighbours(s.smokeNode())) {
                addHint(hints, n, SMOKE_NEIGHBOUR_WEIGHT);
            }
        }

        if (s.wireDetectedA() != null) {
            addHint(hints, s.wireDetectedA(), WIRE_DETECTED_NODE_WEIGHT);
            addHint(hints, s.wireDetectedB(), WIRE_DETECTED_NODE_WEIGHT);
            for (NodeId n : MapDefinition.getNeighbours(s.wireDetectedA())) {
                addHint(hints, n, WIRE_DETECTED_NEIGHBOUR_WEIGHT);
            }
            for (NodeId n : MapDefinition.getNeighbours(s.wireDetectedB())) {
                addHint(hints, n, WIRE_DETECTED_NEIGHBOUR_WEIGHT);
            }
        }

        if (s.beartrapGoneNode() != null) {
            addHint(hints, s.beartrapGoneNode(), BEARTRAP_GONE_NODE_WEIGHT);
            for (NodeId n : MapDefinition.getNeighbours(s.beartrapGoneNode())) {
                addHint(hints, n, BEARTRAP_GONE_NEIGHBOUR_WEIGHT);
            }
        }

        for (NodeId candidate : ctx.birdCandidates) {
            addHint(hints, candidate, BIRD_CANDIDATE_NODE_WEIGHT);
            for (NodeId n : MapDefinition.getNeighbours(candidate)) {
                addHint(hints, n, BIRD_CANDIDATE_NEIGHBOUR_WEIGHT);
            }
        }

        return hints;
    }

    private static void addHint(Map<NodeId, Double> hints, NodeId node, double amount) {
        hints.merge(node, amount, Double::sum);
    }

    // ==================================================================
    //  ITEM USAGE PHASE
    // ==================================================================

    private static void useItemsPhase(
            Game game,
            ItemService itemService,
            BossContext ctx,
            TurnSignals signals
    ) {

        List<ItemType> inventory = game.getBossInventory();

        if (inventory.isEmpty()) {
            return;
        }

        Set<ItemType> usedOnceTypes = EnumSet.noneOf(ItemType.class);
        double useChance = inventory.size() >= MAX_INVENTORY
                ? 1.0
                : ITEM_USE_INITIAL_CHANCE;

        while (true) {

            inventory = game.getBossInventory();
            if (inventory.isEmpty()) {
                break;
            }

            if (RNG.nextDouble() >= useChance) {
                break;
            }

            List<Integer> usableIndices = collectUsableIndices(game, inventory, usedOnceTypes);
            if (usableIndices.isEmpty()) {
                break;
            }

            int chosenIndex = usableIndices.get(RNG.nextInt(usableIndices.size()));
            ItemType chosenItem = inventory.get(chosenIndex);

            if (chosenItem == ItemType.SHEARS) {
                handleShearsUsage(game, itemService, chosenIndex, ctx);
            } else {
                UseItemRequest request = buildItemRequest(game, chosenItem, chosenIndex, signals, ctx);
                try {
                    itemService.useItem(game, false, request);
                } catch (InvalidItemException e) {
                    // the pre-checks should prevent this, but never let a
                    // stray exception break turn resolution
                    break;
                }
                applyPostUseEffects(game, chosenItem, ctx);
            }

            if (ONCE_ONLY_TYPES.contains(chosenItem)) {
                usedOnceTypes.add(chosenItem);
            }

            useChance = Math.max(0.0, useChance - ITEM_USE_DECAY);
        }
    }

    private static List<Integer> collectUsableIndices(
            Game game,
            List<ItemType> inventory,
            Set<ItemType> usedOnceTypes
    ) {

        List<StatusEffect> bossStatuses = game.getBossStatusEffects();
        NodeId bossNode = game.getBoss().getCurrentNode();

        List<Integer> usable = new ArrayList<>();

        for (int i = 0; i < inventory.size(); i++) {
            ItemType item = inventory.get(i);

            if (ONCE_ONLY_TYPES.contains(item) && usedOnceTypes.contains(item)) {
                continue;
            }
            if (item == ItemType.STICK && usedOnceTypes.contains(ItemType.SILENCER)) {
                continue;
            }
            if (item == ItemType.SILENCER && usedOnceTypes.contains(ItemType.STICK)) {
                continue;
            }
            if ((item == ItemType.UMBRELLA || item == ItemType.CAMPFIRE)
                    && isNodeOccupied(game, bossNode, false, MapLayer.OWN_NODE)) {
                continue;
            }
            if ((item == ItemType.NUKE || item == ItemType.NAPALM)
                    && hasStrikeModifier(bossStatuses)) {
                continue;
            }
            if (item == ItemType.WIRE && !hasAnyPlaceableWireEdge(game)) {
                continue;
            }
            if (item == ItemType.BEARTRAP && !hasAnyPlaceableBeartrapNode(game)) {
                continue;
            }

            usable.add(i);
        }

        return usable;
    }

    private static UseItemRequest buildItemRequest(
            Game game,
            ItemType item,
            int index,
            TurnSignals signals,
            BossContext ctx
    ) {
        return switch (item) {
            case WIRE -> {
                NodeId[] edge = pickWireEdge(game, signals, ctx);
                yield new UseItemRequest(index, edge[0], edge[1]);
            }
            case BEARTRAP -> {
                NodeId node = pickBeartrapNode(game, signals, ctx);
                yield new UseItemRequest(index, node, null);
            }
            default -> new UseItemRequest(index, null, null);
        };
    }

    private static void applyPostUseEffects(Game game, ItemType item, BossContext ctx) {
        switch (item) {

            case REVELATION -> ctx.enemyHasBird = game.getPlayerInventory().contains(ItemType.BIRD);

            case BIRD -> ctx.birdCandidates = simulateBirdReveal(game);

            default -> {
                // no extra info to capture for the other item types
            }
        }
    }

    private static void handleShearsUsage(
            Game game,
            ItemService itemService,
            int index,
            BossContext ctx
    ) {

        NodeId bossNode = game.getBoss().getCurrentNode();

        List<MapEffect> before = findReachableEnemyEffects(game, bossNode);

        UseItemRequest request = new UseItemRequest(index, null, null);
        try {
            itemService.useItem(game, false, request);
        } catch (InvalidItemException e) {
            return;
        }

        List<MapEffect> after = findReachableEnemyEffects(game, bossNode);

        for (MapEffect effect : before) {

            boolean stillThere = after.stream().anyMatch(e ->
                    e.getType() == effect.getType()
                            && e.getFirstNode() == effect.getFirstNode()
                            && e.getSecondNode() == effect.getSecondNode()
            );

            if (!stillThere) {
                continue; // this is the one the shears destroyed
            }

            if (effect.getType() == MapEffectType.BEARTRAP) {
                ctx.dangerNodes.add(effect.getFirstNode());
            } else if (effect.getType() == MapEffectType.WIRE) {
                ctx.dangerEdges.add(EdgeKey.of(effect.getFirstNode(), effect.getSecondNode()));
            }
        }
    }

    private static List<MapEffect> findReachableEnemyEffects(Game game, NodeId bossNode) {
        return game.getMapEffects().stream()
                .filter(MapEffect::isBelongsToPlayer)
                .filter(effect ->
                        effect.getType() == MapEffectType.WIRE
                                || effect.getType() == MapEffectType.BEARTRAP
                )
                .filter(effect -> canReachWithShears(bossNode, effect))
                .toList();
    }

    private static boolean canReachWithShears(NodeId currentNode, MapEffect effect) {

        if (effect.getType() == MapEffectType.BEARTRAP) {
            return effect.getFirstNode() == currentNode
                    || MapDefinition.areConnected(currentNode, effect.getFirstNode());
        }

        if (effect.getType() == MapEffectType.WIRE) {
            if (MapDefinition.isTunnel(effect.getFirstNode(), effect.getSecondNode())) {
                return false;
            }
            return MapDefinition.areConnected(currentNode, effect.getFirstNode())
                    || MapDefinition.areConnected(currentNode, effect.getSecondNode());
        }

        return false;
    }

    private static List<NodeId> simulateBirdReveal(Game game) {

        NodeId enemyNode = game.getPlayer().getCurrentNode();

        List<NodeId> allNodes = new ArrayList<>(List.of(NodeId.values()));
        allNodes.remove(enemyNode);
        Collections.shuffle(allNodes, RNG);

        List<NodeId> fakeNodes = allNodes.subList(0, 2);

        List<NodeId> result = new ArrayList<>();
        result.add(enemyNode);
        result.addAll(fakeNodes);
        Collections.shuffle(result, RNG);

        return result;
    }

    private static NodeId[] pickWireEdge(Game game, TurnSignals signals, BossContext ctx) {

        Map<NodeId, Double> hints = computeNodeHints(signals, ctx);

        List<NodeId[]> candidates = getAllNonTunnelEdges().stream()
                .filter(e -> !isEdgeOccupied(game, e[0], e[1], false))
                .toList();

        return weightedPickList(
                candidates,
                edge -> DEFAULT_NODE_WEIGHT
                        + hints.getOrDefault(edge[0], 0.0)
                        + hints.getOrDefault(edge[1], 0.0)
        );
    }

    private static NodeId pickBeartrapNode(Game game, TurnSignals signals, BossContext ctx) {

        Map<NodeId, Double> hints = computeNodeHints(signals, ctx);

        List<NodeId> candidates = Arrays.stream(NodeId.values())
                .filter(n -> !isNodeOccupied(game, n, false, MapLayer.ENEMY_NODE))
                .toList();

        return weightedPickList(
                candidates,
                n -> DEFAULT_NODE_WEIGHT + hints.getOrDefault(n, 0.0)
        );
    }

    private static List<NodeId[]> getAllNonTunnelEdges() {

        List<NodeId[]> edges = new ArrayList<>();

        for (NodeId a : NodeId.values()) {
            for (NodeId b : MapDefinition.getNeighbours(a)) {
                if (a.ordinal() < b.ordinal() && !MapDefinition.isTunnel(a, b)) {
                    edges.add(new NodeId[]{a, b});
                }
            }
        }

        return edges;
    }

    private static boolean hasAnyPlaceableWireEdge(Game game) {
        return getAllNonTunnelEdges().stream()
                .anyMatch(e -> !isEdgeOccupied(game, e[0], e[1], false));
    }

    private static boolean hasAnyPlaceableBeartrapNode(Game game) {
        return Arrays.stream(NodeId.values())
                .anyMatch(n -> !isNodeOccupied(game, n, false, MapLayer.ENEMY_NODE));
    }

    private static boolean isNodeOccupied(Game game, NodeId node, boolean playerSide, MapLayer layer) {
        return game.getMapEffects().stream().anyMatch(effect ->
                effect.getFirstNode() == node
                        && effect.isBelongsToPlayer() == playerSide
                        && effect.getType().getLayer() == layer
        );
    }

    private static boolean isEdgeOccupied(Game game, NodeId a, NodeId b, boolean playerSide) {
        return game.getMapEffects().stream().anyMatch(effect ->
                effect.getType() == MapEffectType.WIRE
                        && effect.isBelongsToPlayer() == playerSide
                        && (
                        (effect.getFirstNode() == a && effect.getSecondNode() == b)
                                || (effect.getFirstNode() == b && effect.getSecondNode() == a)
                )
        );
    }

    private static boolean hasMapEffect(Game game, MapEffectType type, boolean playerSide, NodeId node) {
        return game.getMapEffects().stream().anyMatch(effect ->
                effect.getType() == type
                        && effect.isBelongsToPlayer() == playerSide
                        && effect.getFirstNode() == node
        );
    }

    private static boolean hasStrikeModifier(List<StatusEffect> statuses) {
        return hasStatus(statuses, StatusEffectType.NUKE_CHARGING)
                || hasStatus(statuses, StatusEffectType.NUKE_READY)
                || hasStatus(statuses, StatusEffectType.NAPALM_CHARGING)
                || hasStatus(statuses, StatusEffectType.NAPALM_READY);
    }

    // ==================================================================
    //  LOG ANALYSIS (signals from the previous turn)
    // ==================================================================

    private record TurnSignals(
            boolean justStruck,
            NodeId smokeNode,
            boolean bossRevealedSelfLastTurn,
            boolean justStayedStillLastTurn,
            boolean sleptToChargeLastTurn,
            NodeId wireDetectedA,
            NodeId wireDetectedB,
            NodeId beartrapGoneNode
    ) {
        boolean knowsPlayerPosition() { return smokeNode != null; }
        boolean wireDetectedLastTurn() { return wireDetectedA != null; }
        boolean beartrapGoneLastTurn() { return beartrapGoneNode != null; }
    }

    private static TurnSignals collectTurnSignals(Game game) {

        boolean justStruck = didBossStrikeLastTurn(game);
        NodeId smokeNode = detectPlayerSmokeNode(game);
        boolean revealedSelf = detectBossRevealedSelfLastTurn(game);
        boolean stayedStill = detectBossStayedStillLastTurn(game);
        boolean sleptToCharge = detectBossSleptToChargeLastTurn(game);
        NodeId[] wireDetected = detectWireTriggeredLastTurn(game);
        NodeId beartrapGone = detectBeartrapGoneLastTurn(game);

        return new TurnSignals(
                justStruck,
                smokeNode,
                revealedSelf,
                stayedStill,
                sleptToCharge,
                wireDetected != null ? wireDetected[0] : null,
                wireDetected != null ? wireDetected[1] : null,
                beartrapGone
        );
    }

    private static boolean didBossStrikeLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        return game.getLogs().stream()
                .anyMatch(log ->
                        log.getTurnNumber() == previousTurn
                                && !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You strike at node")
                );
    }

    private static NodeId detectPlayerSmokeNode(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        Optional<LogEntry> smokeLog = game.getLogs().stream()
                .filter(log ->
                        log.getTurnNumber() == previousTurn
                                && !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You see smoke coming from")
                )
                .reduce((first, second) -> second);

        return smokeLog.map(BossBehaviour::extractNodeFromMessage).orElse(null);
    }

    // symmetric to detectPlayerSmokeNode: this fires when the BOSS's own
    // strike/campfire revealed ITS position to the player last turn
    private static boolean detectBossRevealedSelfLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        return game.getLogs().stream()
                .anyMatch(log ->
                        log.getTurnNumber() == previousTurn
                                && log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You see smoke coming from")
                );
    }

    private static boolean detectBossStayedStillLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        return game.getLogs().stream()
                .anyMatch(log ->
                        log.getTurnNumber() == previousTurn
                                && !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You focus all your energy to set yourself free")
                );
    }

    private static boolean detectBossSleptToChargeLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        boolean sleptLastTurn = game.getLogs().stream()
                .anyMatch(log ->
                        log.getTurnNumber() == previousTurn
                                && !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You choose to sleep at node")
                );

        if (!sleptLastTurn) {
            return false;
        }

        boolean gotItemLastTurn = game.getLogs().stream()
                .anyMatch(log ->
                        log.getTurnNumber() == previousTurn
                                && !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You conjured a")
                );

        return !gotItemLastTurn;
    }

    private static NodeId[] detectWireTriggeredLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        Optional<LogEntry> log = game.getLogs().stream()
                .filter(l ->
                        l.getTurnNumber() == previousTurn
                                && !l.getPlayerSide()
                                && l.getMessage() != null
                                && l.getMessage().startsWith("A movement has been detected between")
                )
                .reduce((first, second) -> second);

        if (log.isEmpty()) {
            return null;
        }

        String[] parts = log.get().getMessage().trim().split(" ");

        try {
            NodeId b = NodeId.valueOf(parts[parts.length - 1]);
            NodeId a = NodeId.valueOf(parts[parts.length - 3]);
            return new NodeId[]{a, b};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static NodeId detectBeartrapGoneLastTurn(Game game) {

        int previousTurn = game.getTurnNumber() - 1;

        Optional<LogEntry> log = game.getLogs().stream()
                .filter(l ->
                        l.getTurnNumber() == previousTurn
                                && !l.getPlayerSide()
                                && l.getMessage() != null
                                && l.getMessage().startsWith("A Beartrap mysteriously disappeared from node")
                )
                .reduce((first, second) -> second);

        return log.map(BossBehaviour::extractNodeFromMessage).orElse(null);
    }

    private static NodeId findPreviousBossNode(Game game) {

        List<LogEntry> moveLogs = game.getLogs().stream()
                .filter(log ->
                        !log.getPlayerSide()
                                && log.getMessage() != null
                                && log.getMessage().startsWith("You moved in node")
                )
                .toList();

        if (moveLogs.size() < 2) {
            return null;
        }

        LogEntry secondToLast = moveLogs.get(moveLogs.size() - 2);
        return extractNodeFromMessage(secondToLast);
    }

    private static NodeId extractNodeFromMessage(LogEntry log) {
        String message = log.getMessage();
        String[] parts = message.trim().split(" ");
        String last = parts[parts.length - 1];
        try {
            return NodeId.valueOf(last);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==================================================================
    //  GENERIC HELPERS
    // ==================================================================

    private static boolean hasStatus(List<StatusEffect> effects, StatusEffectType type) {
        return effects.stream().anyMatch(e -> e.getType() == type);
    }

    private static <T> T weightedPick(Map<T, Double> weights) {

        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = RNG.nextDouble() * total;

        double cumulative = 0;
        for (Map.Entry<T, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }

        return weights.keySet().iterator().next();
    }

    private static <T> T weightedPickList(List<T> items, ToDoubleFunction<T> weightFn) {

        double[] weights = new double[items.size()];
        double total = 0;

        for (int i = 0; i < items.size(); i++) {
            weights[i] = Math.max(0.0001, weightFn.applyAsDouble(items.get(i)));
            total += weights[i];
        }

        double roll = RNG.nextDouble() * total;
        double cumulative = 0;

        for (int i = 0; i < items.size(); i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                return items.get(i);
            }
        }

        return items.get(items.size() - 1);
    }

    private record EdgeKey(NodeId a, NodeId b) {
        static EdgeKey of(NodeId x, NodeId y) {
            return x.ordinal() <= y.ordinal() ? new EdgeKey(x, y) : new EdgeKey(y, x);
        }
    }

    private static final class BossContext {
        boolean enemyHasBird = false;
        List<NodeId> birdCandidates = new ArrayList<>();
        Set<NodeId> dangerNodes = new HashSet<>();
        Set<EdgeKey> dangerEdges = new HashSet<>();
    }
}