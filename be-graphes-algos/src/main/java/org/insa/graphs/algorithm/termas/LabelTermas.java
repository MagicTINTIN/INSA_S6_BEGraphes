package org.insa.graphs.algorithm.termas;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Point;

public class LabelTermas implements Comparable<LabelTermas> {
    private Node currentVertex;
    private Node center, destination;
    private float distanceToCenter, radius, minRadius, maxRadius;
    private float orientation;
    private float fitnessWeight, shortestDistance;
    private boolean accessible;
    private boolean marked;
    private boolean reached;
    protected float realisedCost;
    private Arc parent;
    private int ID;

    public double distanceToGeometryWeight(Node center, Node position, double radius) {
        return distanceToCircleWeight(center, position, radius);
        // return distanceToStarWeight(center, position, radius);
        //return distanceToRectangleWeight(center, position, radius);
    }

    public LabelTermas(Node defaultVertex, Node center, float min, float max, float radius) {
        this.currentVertex = defaultVertex;
        this.marked = false;
        this.realisedCost = Float.MAX_VALUE;
        this.parent = null;
        this.ID = defaultVertex.getId();
        this.reached = false;
        this.center = center;
        this.destination = null;
        this.radius = radius;
        this.minRadius = min;
        this.maxRadius = max;
        this.orientation = (float) getOrientation(center.getPoint(), defaultVertex.getPoint());
        this.distanceToCenter = distanceToCenter(center, defaultVertex);
        if (this.distanceToCenter >= minRadius && this.distanceToCenter <= maxRadius) {
            this.fitnessWeight = maxRadius * (float) distanceToGeometryWeight(center, defaultVertex, (double) radius);
            this.shortestDistance = Float.MAX_VALUE;
            this.accessible = true;
        } else {
            this.fitnessWeight = Float.MAX_VALUE;
            this.shortestDistance = Float.MAX_VALUE;
            this.accessible = false;
        }

    }

    public LabelTermas(Node cVertex, Node destination, boolean m, boolean r, float cost, Arc par, Node center, float min, float max,
            float radius) {
        this(cVertex, center, min, max, radius);
        this.destination = destination;
        this.marked = m;
        this.realisedCost = cost;
        this.parent = par;
        this.reached = r;

        if (this.accessible) {
            this.shortestDistance = distanceToCenter(destination, cVertex);
        }
    }

    public double distanceToCircleWeight(Node center, Node position, double radius) {
        return 0 * Math.pow(Math.abs(Point.distance(center.getPoint(), position.getPoint()) - radius), 1.);
    }

    public static double getOrientation(Point o, Point a) {
        double latO = Math.toRadians(o.getLatitude());
        double lonO = Math.toRadians(o.getLongitude());
        double latA = Math.toRadians(a.getLatitude());
        double lonA = Math.toRadians(a.getLongitude());

        double distanceOA = o.distanceTo(a) / Point.EARTH_RADIUS; // Central angle
        double initialAngle = Math.atan2(Math.sin(lonA - lonO) * Math.cos(latA),
                                         Math.cos(latO) * Math.sin(latA) - Math.sin(latO) * Math.cos(latA) * Math.cos(lonA - lonO));
        return initialAngle;
    }

    public static Point calculatePointOnCircle(Point o, Point a, double angle) {
        double latO = Math.toRadians(o.getLatitude());
        double lonO = Math.toRadians(o.getLongitude());
        double latA = Math.toRadians(a.getLatitude());
        double lonA = Math.toRadians(a.getLongitude());

        double distanceOA = o.distanceTo(a) / Point.EARTH_RADIUS; // Central angle
        double initialAngle = Math.atan2(Math.sin(lonA - lonO) * Math.cos(latA),
                                         Math.cos(latO) * Math.sin(latA) - Math.sin(latO) * Math.cos(latA) * Math.cos(lonA - lonO));

        double newAngle = initialAngle + angle;

        double newLat = Math.asin(Math.sin(latO) * Math.cos(distanceOA) +
                                  Math.cos(latO) * Math.sin(distanceOA) * Math.cos(newAngle));
        double newLon = lonO + Math.atan2(Math.sin(newAngle) * Math.sin(distanceOA) * Math.cos(latO),
                                          Math.cos(distanceOA) - Math.sin(latO) * Math.sin(newLat));

        return new Point((float) Math.toDegrees(newLon), (float) Math.toDegrees(newLat));
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

    public float getFitness() {
        return this.fitnessWeight;
    }

    public boolean hasBeenReached() {
        return this.reached;
    }

    public void reset() {
        this.marked = false;
        this.reached = false;
        this.realisedCost = Float.MAX_VALUE;
        this.parent = null;
        this.destination = null;
    }

    public void setDestination(Node destination) {
        if (this.accessible) {
            this.destination = destination;
            this.shortestDistance = (float) Math.pow(distanceToCenter(destination, this.currentVertex), 1);
        }
    }

    public boolean isReached() {
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
        this.parent = null;
        this.reached = true;
        this.realisedCost = newCost;
        this.parent = newParent;
    }

    public float getTotalCost() {
        return (float) Math.pow(this.realisedCost, 1) + fitnessWeight + shortestDistance;
    }

    // public float getMinCircleArcLength() {
    //     float minRadius =distanceToCenter(center, destination);// (float) Math.min(distanceToCenter(center, destination), distanceToCenter);
    //     float diffOrientation = (float) Math.abs((orientation - getOrientation(center.getPoint(), destination.getPoint())) % (2*Math.PI));
    //     return minRadius * diffOrientation;
    // }

    // public float getTotalCost() {
    //     return (float) Math.pow(this.realisedCost, 1) + getMinCircleArcLength();
    // }

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

    public float getOrientation() {
        return this.orientation;
    }

    @Override
    public int compareTo(LabelTermas o) {
        return Float.compare(this.getTotalCost(), o.getTotalCost());
    }
}
