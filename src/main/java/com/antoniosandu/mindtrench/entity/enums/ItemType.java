package com.antoniosandu.mindtrench.entity.enums;

public enum ItemType {
    STICK(
            "Stick",
            "Makes you move 2 times in one turn."
    ),
    WIRE(
            "Wire",
            "Place in on an enemy edge, it will tell you if the virus passed through that edge on the next turn."
    ),
    UMBRELLA(
            "Umbrella",
            "Place it on your current position, it will shield you from enemy strikes for one turn."
    ),
    CAMPFIRE(
            "Campfire",
            "Place it on your current position, if you sleep this turn it will be like sleeping two turns, though it will reveal your position."
    ),
    BEARTRAP(
            "Beartrap",
            "Place it on an enemy node, if the virus passes on that node the beartrap will snap, stunning it for one turn (you will receive the information one turn later)."
    ),
    SHEARS(
            "Shears",
            "Will reveal items that the virus placed on your map near you, it will destroy one item of your choosing."
    ),
    BIRD(
            "Bird",
            "Send the bird to gather information on the virus' position, it will tell you three nodes, one of them is its current position."
    ),
    SILENCER(
            "Silencer",
            "Make your next strike silent."
    ),
    REVELATION(
            "Revelation",
            "Use it to gather information on the virus' inventory."
    ),
    //PARADOX(
    //            "Paradox",
    //            "From next turn, swap maps with the enemy for two turns."
    //    ),
    NUKE(
            "Nuke",
            "[Requires 3 turns of charging] Once charged, your next strike will hit the node selected and all the others around it (tunnels do not count)."
    ),
    NAPALM(
            "Napalm",
            "[Requires 2 turns of charging] Once charged, your next strike will last on the selected node for a second turn, damaging the virus if it steps on that node."
    );

    private final String displayName;
    private final String description;
    ItemType(
        String displayName,
        String description
    ) {
        this.displayName = displayName;
        this.description = description;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }
}

