package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class Label implements Comparable<Label> {
    private Node currentVertex;
    private boolean marked;
    private boolean reached;
    protected float realisedCost;
    private Arc parent;
    private int ID;

    public Label(Node cVertex, boolean m, boolean r, float cost, Arc par)
    {
        this.currentVertex = cVertex;
        this.marked = m;
        this.realisedCost = cost;
        this.parent = par;
        this.ID = cVertex.getId();
        this.reached = r;
    }

    public Label(Node defaultVertex)
    {
        this.currentVertex = defaultVertex;
        this.marked = false;
        this.realisedCost = Float.MAX_VALUE;
        this.parent = null;
        this.ID = defaultVertex.getId();
        this.reached = false;
    }

    public int getID()
    {
        return this.ID;
    }

    public float getCost()
    {
        return this.realisedCost;
    }

    public boolean hasBeenReached()
    {
        return this.reached;
    }

    public void reach()
    {
        this.reached = true;
    }

    /**
     * 
     * @param newCost
     * @param parent
     * @return returns 0 if updated, 1 if cost not updated (new is >), 2 if already marked
     */
    public int updateCostAndParent(float newCost, Arc newParent)
    {
        this.reached = true;
        if (marked)
            return 2;
        if (newCost > this.realisedCost)
            return 1;
        this.realisedCost = newCost;
        this.parent = newParent;
        return 0;
    }

    public void setCost(float newCost)
    {
        this.reached = true;
        this.realisedCost = newCost;
    }

    public void setCostAndParent(float newCost, Arc newParent)
    {
        this.reached = true;
        this.realisedCost = newCost;
        this.parent = newParent;
    }

    public float getTotalCost()
    {
        return this.realisedCost;
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

    public Arc getParent()
    {
        return this.parent;
    }

    public Node getCurrentVertex()
    {
        return this.currentVertex;
    }


    @Override
    public int compareTo(Label o) {
        return Float.compare(this.getTotalCost(), o.getTotalCost());
    }
}
