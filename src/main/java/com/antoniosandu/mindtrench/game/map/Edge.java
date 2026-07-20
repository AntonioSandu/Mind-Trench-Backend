package com.antoniosandu.mindtrench.game.map;

public class Edge {

    private final NodeId first;

    private final NodeId second;

    private final boolean tunnel;

    public Edge(
            NodeId first,
            NodeId second,
            boolean tunnel
    ) {
        this.first = first;
        this.second = second;
        this.tunnel = tunnel;
    }

    public NodeId getFirst() {
        return first;
    }

    public NodeId getSecond() {
        return second;
    }

    public boolean isTunnel() {
        return tunnel;
    }

    public boolean connects(
            NodeId a,
            NodeId b
    ) {
        return (first == a && second == b)
                || (first == b && second == a);
    }
}