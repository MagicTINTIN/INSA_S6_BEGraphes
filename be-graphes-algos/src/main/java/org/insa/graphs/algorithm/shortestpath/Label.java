package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class Label implements Comparable<Label> {
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

    public Label(Node defaultVertex)
    {
        this.currentVertex = defaultVertex;
        this.marked = false;
        this.realisedCost = Double.MAX_VALUE;
        this.parent = null;
    }

    public double getCost()
    {
        return this.realisedCost;
    }

    /**
     * 
     * @param newCost
     * @param parent
     * @return returns 0 if updated, 1 if cost not updated (new is >), 2 if already marked
     */
    public int updateCostAndParent(double newCost, Node newParent)
    {
        if (marked)
            return 2;
        if (newCost > this.realisedCost)
            return 1;
        this.realisedCost = newCost;
        this.parent = newParent;
        return 0;
    }

    public void setCost(double newCost)
    {
        this.realisedCost = newCost;
    }

    public boolean isMarked()
    {
        return this.marked;
    }

    public void mark()
    {
        this.marked = true;
    }

    public void unmark()
    {
        this.marked = false;
    }

    public Node getParent()
    {
        return this.parent;
    }

    public Node getCurrentVertex()
    {
        return this.currentVertex;
    }


    @Override
    public int compareTo(Label o) {
        return Double.compare(this.realisedCost, o.realisedCost);
    }
}
