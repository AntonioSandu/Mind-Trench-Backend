package com.antoniosandu.mindtrench.entity.enums;

public enum MapEffectType {

    UMBRELLA(MapLayer.OWN_NODE),

    CAMPFIRE(MapLayer.OWN_NODE),

    BEARTRAP(MapLayer.ENEMY_NODE),

    WIRE(MapLayer.ENEMY_EDGE),

    NAPALM_SECOND_HIT(MapLayer.ENEMY_NODE);

    private final MapLayer layer;

    MapEffectType(MapLayer layer){
        this.layer = layer;
    }

    public MapLayer getLayer(){
        return this.layer;
    }
}
