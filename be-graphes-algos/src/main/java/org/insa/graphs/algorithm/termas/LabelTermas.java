package org.insa.graphs.algorithm.termas;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Point;

public class LabelTermas implements Comparable<LabelTermas> {
    private Node currentVertex;
    private Node center;
    private float distanceToCenter, radius, minRadius, maxRadius;
    private float fitnessWeight;
    private boolean accessible;
    private boolean marked;
    private boolean reached;
    protected float realisedCost;
    private Arc parent;
    private int ID;

    public double distanceToGeometryWeight(Node center, Node position, double radius) {
        return distanceToCircleWeight(center, position, radius);
    }

    public LabelTermas(Node defaultVertex, Node center, float min, float max, float radius) {
        this.currentVertex = defaultVertex;
        this.marked = false;
        this.realisedCost = Float.MAX_VALUE;
        this.parent = null;
        this.ID = defaultVertex.getId();
        this.reached = false;
        this.center = center;
        this.radius = radius;
        this.minRadius = min;
        this.maxRadius = max;
        this.distanceToCenter = distanceToCenter(center, defaultVertex);
        if (this.distanceToCenter >= minRadius && this.distanceToCenter <= maxRadius) {
            this.fitnessWeight = maxRadius * (float) distanceToGeometryWeight(center, defaultVertex, (double) radius);
            this.accessible = true;
        } else {
            this.fitnessWeight = Float.MAX_VALUE;
            this.accessible = false;
        }

    }

    public LabelTermas(Node cVertex, boolean m, boolean r, float cost, Arc par, Node center, float min, float max,
            float radius) {
        this(cVertex, center, min, max, radius);
        this.marked = m;
        this.realisedCost = cost;
        this.parent = par;
        this.reached = r;
    }

    public double distanceToCircleWeight(Node center, Node position, double radius) {
        return Math.pow(Point.distance(center.getPoint(), position.getPoint()) - radius, 4);
    }

    public float distanceToCenter(Node center, Node position) {
        return (float) Point.distance(center.getPoint(), position.getPoint());
    }

    public int getID() {
        return this.ID;
    }

    public float getCost() {
        return this.realisedCost;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public boolean hasBeenReached() {
        return this.reached;
    }

    public void reach() {
        this.reached = true;
    }

    /**
     * 
     * @param newCost
     * @param parent
     * @return returns 0 if updated, 1 if cost not updated (new is >), 2 if already
     *         marked
     */
    public int updateCostAndParent(float newCost, Arc newParent) {
        this.reached = true;
        if (marked)
            return 2;
        if (newCost > this.realisedCost)
            return 1;
        this.realisedCost = newCost;
        this.parent = newParent;
        return 0;
    }

    public void setCost(float newCost) {
        this.reached = true;
        this.realisedCost = newCost;
    }

    public void setCostAndParent(float newCost, Arc newParent) {
        this.reached = true;
        this.realisedCost = newCost;
        this.parent = newParent;
    }

    public float getTotalCost() {
        return this.realisedCost + fitnessWeight;
    }

    public boolean isMarked() {
        return this.marked;
    }

    public void mark() {
        this.marked = true;
    }

    public void unmark() {
        this.marked = false;
    }

    public Arc getParent() {
        return this.parent;
    }

    public Node getCurrentVertex() {
        return this.currentVertex;
    }

    @Override
    public int compareTo(LabelTermas o) {
        return Float.compare(this.getTotalCost(), o.getTotalCost());
    }
}
