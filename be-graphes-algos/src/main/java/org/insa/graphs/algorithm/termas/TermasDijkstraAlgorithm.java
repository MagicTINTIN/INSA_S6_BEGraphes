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
    private float minRadius;
    private float maxRadius;

    public TermasDijkstraAlgorithm(TermasData data) {
        super(data);
    }

    protected LabelTermas createLabel(Node n, Node c, float min, float max, float radius) {
        return new LabelTermas(n, c, min, max, radius);
    }

    private ArrayList<Arc> dijkstraAlgo(Graph graph, LabelTermas nodeLabels[], TermasData data, int startID, Node firstDestination, int firstDestinationID) {
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
                    //if (successorLabel.getCost() != Float.MAX_VALUE)
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
            if (goingBack.getParent() != null)
                System.out.println("Chelou ça: " + goingBack.getParent().getOrigin().getId());
            else
                System.out.println("Pas chelou: " + goingBack.getParent());
        }
        Collections.reverse(shortestArcs);
        return shortestArcs;
    }

    @Override
    protected TermasSolution doRun() {
        final TermasData data = getInputData();
        final double minRadius = data.getMin();
        final double maxRadius = data.getMax();
        final int centerID = data.getCenter().getId();
        final int startID = data.getStart().getId();
        TermasSolution solution = null;

        // initialising
        Graph graph = data.getGraph();
        final int nbNodes = graph.size();
        LabelTermas nodeLabels[] = new LabelTermas[nbNodes];

        System.out.println("Center : x/lon: " + data.getCenter().getPoint().getLongitude() + ", y/lat: " + data.getCenter().getPoint().getLatitude());
        System.out.println("Start : x/lon: " + data.getStart().getPoint().getLongitude() + ", y/lat: " + data.getStart().getPoint().getLatitude());

        Point firstIdealPosition = LabelTermas.calculatePointOnCircle(data.getCenter().getPoint(), data.getStart().getPoint(), 2*Math.PI/3);
        System.out.println("1st Ideal : x/lon: " + firstIdealPosition.getLongitude() + ", y/lat: " + firstIdealPosition.getLatitude());
        Node firstDestination = null;
        float firstFitness = Float.MAX_VALUE;

        Point secondIdealPosition = LabelTermas.calculatePointOnCircle(data.getCenter().getPoint(), data.getStart().getPoint(), -2*Math.PI/3);
        System.out.println("2nd Ideal : x/lon: " + secondIdealPosition.getLongitude() + ", y/lat: " + secondIdealPosition.getLatitude());
        Node secondDestination = null;
        float secondFitness = Float.MAX_VALUE;

        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()] = createLabel(node, data.getCenter(), (float) data.getMin(), (float) data.getMax(), (float) data.getRadius());
            if (nodeLabels[node.getId()].isAccessible()) {
                float dTo1 = (float) Point.distance(node.getPoint(), firstIdealPosition);
                float dTo2 = (float) Point.distance(node.getPoint(), secondIdealPosition);
                if (dTo1 < firstFitness) {
                    firstDestination = node;
                    firstFitness = dTo1;
                }
                if (dTo2 < secondFitness) {
                    secondDestination = node;
                    secondFitness = dTo2;
                }
            }
        }


        if (firstDestination == null || firstFitness == Float.MAX_VALUE || secondDestination == null || secondFitness == Float.MAX_VALUE
            || firstDestination.getId() == data.getStart().getId()
            || secondDestination.getId() == firstDestination.getId()
            || data.getStart().getId() == secondDestination.getId()) {
            solution = new TermasSolution(data, Status.INFEASIBLE);
            return solution;
        }

        System.out.println("1st Real : x/lon: " + firstDestination.getPoint().getLongitude() + ", y/lat: " + firstDestination.getPoint().getLatitude());
        System.out.println("2nd Real : x/lon: " + secondDestination.getPoint().getLongitude() + ", y/lat: " + secondDestination.getPoint().getLatitude());
        final int firstDestinationID = firstDestination.getId();
        final int secondDestinationID = secondDestination.getId();

        notifyOriginProcessed(data.getStart());

        ArrayList<Arc> shortestArcs = new ArrayList<>();
        ArrayList<Arc> tmpArcs = dijkstraAlgo(graph, nodeLabels, data, startID, firstDestination, firstDestinationID);
        if (tmpArcs == null) {
            solution = new TermasSolution(data, Status.INFEASIBLE);
            return solution;
        }
        else {
            shortestArcs.addAll(tmpArcs);
        }

        tmpArcs = dijkstraAlgo(graph, nodeLabels, data, firstDestinationID, secondDestination, secondDestinationID);
        if (tmpArcs == null) {
            solution = new TermasSolution(data, Status.INFEASIBLE);
            return solution;
        }
        else {
            shortestArcs.addAll(tmpArcs);
        }

        tmpArcs = dijkstraAlgo(graph, nodeLabels, data, secondDestinationID, data.getStart(), startID);
        if (tmpArcs == null) {
            solution = new TermasSolution(data, Status.INFEASIBLE);
            return solution;
        }
        else {
            shortestArcs.addAll(tmpArcs);
        }

        // for (Node node : graph.getNodes()) {
        //     nodeLabels[node.getId()].setDestination(secondDestination);
        // }

        notifyDestinationReached(firstDestination);
        
        // if (goingBack.getParent() != null) {
        //     shortestArcs.add(goingBack.getParent());
        //     goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        // }
        solution = new TermasSolution(data, Status.OPTIMAL, new Path(graph, shortestArcs));
        return solution;
    }

}
