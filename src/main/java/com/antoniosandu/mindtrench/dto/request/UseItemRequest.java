package com.antoniosandu.mindtrench.dto.request;

import com.antoniosandu.mindtrench.entity.enums.ItemType;
import com.antoniosandu.mindtrench.game.map.NodeId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UseItemRequest {

    @NotNull
    @Min(0)
    private Integer inventoryIndex;

    private NodeId firstNode;

    private NodeId secondNode;

    public Integer getInventoryIndex() {
        return inventoryIndex;
    }

    public void setInventoryIndex(Integer inventoryIndex) {
        this.inventoryIndex = inventoryIndex;
    }

    public NodeId getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(NodeId firstNode) {
        this.firstNode = firstNode;
    }

    public NodeId getSecondNode() {return secondNode; }

    public void setSecondNode(NodeId secondNode){
        this.secondNode = secondNode;
    }

}