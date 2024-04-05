package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Node;

public class Label {
    private Node currentVertex;
    private boolean marked;
    private double realisedCost;
    private Node parent;

    public Label(Node cVertex, boolean m, double cost, Node par)
    {
        this.currentVertex = cVertex;
        this.marked = m;
        this.realisedCost = cost;
        this.parent = par;
    }
}
