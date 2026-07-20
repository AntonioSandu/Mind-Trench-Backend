package com.antoniosandu.mindtrench.dto.request;

import com.antoniosandu.mindtrench.game.action.ActionType;
import com.antoniosandu.mindtrench.game.map.NodeId;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TurnRequest {

    @NotNull
    private ActionType actionType;

    private List<NodeId> targetNodes;

    public TurnRequest(){}

    public TurnRequest(ActionType actionType, List<NodeId> targetNodes){
        this.actionType=actionType;
        this.targetNodes=targetNodes;
    }

    public ActionType getActionType(){
        return this.actionType;
    }

    public void setActionType(ActionType actionType){
        this.actionType = actionType;
    }

    public List<NodeId> getTargetNodes(){
        return this.targetNodes;
    }

    public void setTargetNodes (List<NodeId> targetNodes){
        this.targetNodes=targetNodes;
    }
}