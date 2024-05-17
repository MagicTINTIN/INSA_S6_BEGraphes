package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Point;

public class LabelStar extends Label {
    private float shortestDistance;
    
    public LabelStar(Node cVertex, boolean m, boolean r, float cost, Arc par, Node destVertex, int maxSpeed)
    {
        super(cVertex, m, r, cost, par);
        shortestDistance = processShortestDistance(cVertex, destVertex, maxSpeed);
    }

    public LabelStar(Node defaultVertex, Node destVertex, int maxSpeed)
    {
        super(defaultVertex);
        shortestDistance = processShortestDistance(defaultVertex, destVertex, maxSpeed);
    }

    public float processShortestDistance(Node start, Node end, int maxSpeed) {
        if (maxSpeed != -1)
            return (float) Point.distance(start.getPoint(), end.getPoint()) / (1000*maxSpeed);
        return (float) Point.distance(start.getPoint(), end.getPoint());
    }

    @Override
    public float getTotalCost()
    {
        return this.realisedCost + shortestDistance;
    }
}
