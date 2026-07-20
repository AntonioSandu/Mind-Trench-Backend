package com.antoniosandu.mindtrench.game.map;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class MapDefinition {

    private static final List<Edge> EDGES = List.of(
            new Edge(NodeId.A, NodeId.B, false),
            new Edge(NodeId.A, NodeId.C, false),
            new Edge(NodeId.B, NodeId.C, false),
            new Edge(NodeId.B, NodeId.F, true),
            new Edge(NodeId.C, NodeId.D, false),
            new Edge(NodeId.D, NodeId.E, false),
            new Edge(NodeId.E, NodeId.F, false),
            new Edge(NodeId.E, NodeId.G, false),
            new Edge(NodeId.F, NodeId.G, false)
    );

    private MapDefinition() {}

    public static boolean areConnected(
            NodeId a,
            NodeId b
    ) {
        for (Edge edge : EDGES) {
            if (edge.connects(a, b)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTunnel(
            NodeId a,
            NodeId b
    ) {
        for (Edge edge : EDGES) {
            if (edge.connects(a, b)) {
                return edge.isTunnel();
            }
        }
        return false;
    }

    public static Set<NodeId> getNeighbours(
            NodeId node
    ) {
        Set<NodeId> neighbours = new HashSet<>();
        for (Edge edge : EDGES) {
            if (edge.getFirst() == node) {
                neighbours.add(edge.getSecond());
            }
            if (edge.getSecond() == node) {
                neighbours.add(edge.getFirst());
            }
        }
        return neighbours;
    }

    private static final Random RANDOM = new Random();

    public static NodeId getRandomNode() {

        NodeId[] nodes = NodeId.values();

        return nodes[RANDOM.nextInt(nodes.length)];
    }
}
