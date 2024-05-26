package org.insa.graphs.algorithm.termas;

import org.insa.graphs.algorithm.AbstractInputData;
import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Point;

public class TermasData extends AbstractInputData {

    // Origin and start nodes.
    private final Node center, start;
    private final double minRadius, maxRadius;

    /**
     * Construct a new instance of ShortestPathInputData with the given parameters.
     * 
     * @param graph Graph in which the path should be looked for.
     * @param center Center node of the circle.
     * @param start Start node of the path.
     * @param arcInspector Filter for arcs (used to allow only a specific set of
     *        arcs in the graph to be used).
     */
    public TermasData(Graph graph, Node center, Node start, Node minNode, Node maxNode, ArcInspector arcInspector) {
        super(graph, arcInspector);
        this.center = center;
        this.start = start;
        this.minRadius = Point.distance(center.getPoint(), minNode.getPoint());
        this.maxRadius = Point.distance(center.getPoint(), maxNode.getPoint());
    }

    /**
     * @return Center node for the path.
     */
    public Node getCenter() {
        return center;
    }

    /**
     * @return Start node for the path.
     */
    public Node getStart() {
        return start;
    }

    /**
     * @return Min distance from the center.
     */
    public double getMin() {
        return minRadius;
    }

    /**
     * @return Max distance from the center.
     */
    public double getMax() {
        return maxRadius;
    }

    @Override
    public String toString() {
        return "Circle circuit around #" + center.getId() + " starting by #" + start.getId() + "(min: " + minRadius + ", max: " + maxRadius + ") ["
                + this.arcInspector.toString().toLowerCase() + "]";
    }
}
