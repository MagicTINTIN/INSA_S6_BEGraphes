package org.insa.graphs.algorithm.termas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.GraphStatistics;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.Point;

public class TermasDijkstraAlgorithm extends TermasAlgorithm {

    public TermasDijkstraAlgorithm(TermasData data) {
        super(data);
    }

    protected LabelTermas createLabel(Node n, Node c, float min, float max, float radius) {
        return new LabelTermas(n, c, min, max, radius);
    }

    private ArrayList<Arc> dijkstraAlgo(Graph graph, LabelTermas nodeLabels[], TermasData data, int startID,
            Node firstDestination, int firstDestinationID) {
        boolean isDestinationMarked = false;
        BinaryHeap<LabelTermas> heap = new BinaryHeap<>();

        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()].reset();
            nodeLabels[node.getId()].setDestination(firstDestination);
        }

        nodeLabels[startID].setCost(0);
        heap.insert(nodeLabels[startID]);

        while (!heap.isEmpty() && !isDestinationMarked) {
            LabelTermas minVertex = heap.deleteMin();

            nodeLabels[minVertex.getID()].mark();
            if (minVertex.getID() == firstDestinationID) {
                isDestinationMarked = true;
                break;
            }
            Node minNode = minVertex.getCurrentVertex();
            notifyNodeMarked(minNode);

            for (Arc arc : minNode.getSuccessors()) {
                Node successor = arc.getDestination();

                if (!data.isAllowed(arc)) {
                    continue;
                }

                LabelTermas successorLabel = nodeLabels[successor.getId()];

                if (!successorLabel.isAccessible()) {
                    continue;
                }

                if (!successorLabel.isMarked()) {
                    // if (successorLabel.getCost() != Float.MAX_VALUE)
                    if (successorLabel.isReached())
                        heap.remove(successorLabel);
                    else
                        notifyNodeReached(successor);

                    int res = successorLabel.updateCostAndParent(minVertex.getCost() + (float) data.getCost(arc),
                            arc);

                    heap.insert(successorLabel);
                }
            }
        }

        if (!isDestinationMarked)
            return null;
        ArrayList<Arc> shortestArcs = new ArrayList<>();
        LabelTermas goingBack = nodeLabels[firstDestinationID];

        while (goingBack.getParent() != null && goingBack.getParent().getOrigin().getId() != startID) {
            shortestArcs.add(goingBack.getParent());
            goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        }
        if (goingBack.getParent() != null) {
            shortestArcs.add(goingBack.getParent());
            goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        }
        Collections.reverse(shortestArcs);
        return shortestArcs;
    }

    @Override
    protected TermasSolution doRun() {
        final int numberOfSteps = 7;
        final TermasData data = getInputData();
        final double minRadius = data.getMin();
        final double maxRadius = data.getMax();
        final int centerID = data.getCenter().getId();
        final int startID = data.getStart().getId();
        TermasSolution solution = null;

        System.out.println("Radius: " + data.getRadius());
        // initialising
        Graph graph = data.getGraph();
        final int nbNodes = graph.size();
        LabelTermas nodeLabels[] = new LabelTermas[nbNodes];

        System.out.println("Center : x/lon: " + data.getCenter().getPoint().getLongitude() + ", y/lat: "
                + data.getCenter().getPoint().getLatitude());
        System.out.println("Start : x/lon: " + data.getStart().getPoint().getLongitude() + ", y/lat: "
                + data.getStart().getPoint().getLatitude());

        Point idealPositions[] = new Point[numberOfSteps - 1];
        float fitnesses[] = new float[numberOfSteps - 1];
        Node destinations[] = new Node[numberOfSteps - 1];
        for (int i = 1; i < numberOfSteps; i++) {
            idealPositions[i - 1] = LabelTermas.calculatePointOnCircle(data.getCenter().getPoint(),
                    data.getStart().getPoint(), 2 * i * Math.PI / numberOfSteps);
            System.out.println(i + " Ideal : x/lon: " + idealPositions[i - 1].getLongitude() + ", y/lat: "
                    + idealPositions[i - 1].getLatitude());
            destinations[i - 1] = null;
            fitnesses[i - 1] = Float.MAX_VALUE;
        }

        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()] = createLabel(node, data.getCenter(), (float) data.getMin(), (float) data.getMax(),
                    (float) data.getRadius());
            if (nodeLabels[node.getId()].isAccessible()) {
                for (int i = 1; i < numberOfSteps; i++) {
                    float dTo = (float) Point.distance(node.getPoint(), idealPositions[i - 1]);
                    if (dTo < fitnesses[i - 1]) {
                        destinations[i - 1] = node;
                        fitnesses[i - 1] = dTo;
                    }
                }
            }
        }

        // checking that every passby exists
        for (int i = 1; i < numberOfSteps; i++) {
            if (destinations[i - 1] == null || fitnesses[i - 1] == Float.MAX_VALUE) {
                solution = new TermasSolution(data, Status.INFEASIBLE);
                return solution;
            }
            System.out.println(i + " Real : x/lon: " + destinations[i - 1].getPoint().getLongitude() + ", y/lat: "
                    + destinations[i - 1].getPoint().getLatitude());
        }

        notifyOriginProcessed(data.getStart());

        ArrayList<Arc> shortestArcs = new ArrayList<>();

        for (int step = 0; step < numberOfSteps; step++) {
            ArrayList<Arc> tmpArcs;
            if (step == 0) {
                if (startID == destinations[step].getId())
                    continue;
                tmpArcs = dijkstraAlgo(graph, nodeLabels, data, startID, destinations[step], destinations[step].getId());
                if (tmpArcs == null) {
                    System.err.println("Failed at step " + step);
                    solution = new TermasSolution(data, Status.INFEASIBLE);
                    return solution;
                } else {
                    shortestArcs.addAll(tmpArcs);
                }
            }
            else if (step == (numberOfSteps - 1)) {
                if (startID == destinations[step - 1].getId())
                    continue;
                tmpArcs = dijkstraAlgo(graph, nodeLabels, data, destinations[step - 1].getId(), data.getStart(), startID);
                if (tmpArcs == null) {
                    System.err.println("Failed at step " + step);
                    solution = new TermasSolution(data, Status.INFEASIBLE);
                    return solution;
                } else {
                    shortestArcs.addAll(tmpArcs);
                }
            }
            else {
                if (destinations[step -1].getId() == destinations[step].getId())
                    continue;
                tmpArcs = dijkstraAlgo(graph, nodeLabels, data, destinations[step - 1].getId(), destinations[step], destinations[step].getId());
                if (tmpArcs == null) {
                    System.err.println("Failed at step " + step);
                    solution = new TermasSolution(data, Status.INFEASIBLE);
                    return solution;
                } else {
                    shortestArcs.addAll(tmpArcs);
                }
            }
        }
        // for (Node node : graph.getNodes()) {
        // nodeLabels[node.getId()].setDestination(secondDestination);
        // }

        notifyDestinationReached(data.getStart());

        // if (goingBack.getParent() != null) {
        // shortestArcs.add(goingBack.getParent());
        // goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        // }
        solution = new TermasSolution(data, Status.OPTIMAL, new Path(graph, shortestArcs));
        return solution;
    }

}
