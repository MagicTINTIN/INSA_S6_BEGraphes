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

    @Override
    protected TermasSolution doRun() {
        final TermasData data = getInputData();
        final double minRadius = data.getMin();
        final double maxRadius = data.getMax();
        final int centerID = data.getCenter().getId();
        final int startID = data.getStart().getId();
        boolean isDestinationMarked = false;
        TermasSolution solution = null;

        // initialising
        Graph graph = data.getGraph();
        final int nbNodes = graph.size();
        LabelTermas nodeLabels[] = new LabelTermas[nbNodes];
        BinaryHeap<LabelTermas> heap = new BinaryHeap<>();

        System.out.println("Center : x/lon: " + data.getCenter().getPoint().getLongitude() + ", y/lat: " + data.getCenter().getPoint().getLatitude());
        System.out.println("Start : x/lon: " + data.getStart().getPoint().getLongitude() + ", y/lat: " + data.getStart().getPoint().getLatitude());

        Point firstIdealPosition = LabelTermas.calculatePointOnCircle(data.getCenter().getPoint(), data.getStart().getPoint(), (float)data.getRadius(), 2*Math.PI/3);
        System.out.println("1st Ideal : x/lon: " + firstIdealPosition.getLongitude() + ", y/lat: " + firstIdealPosition.getLatitude());
        Node firstDestination = null;
        float firstFitness = Float.MAX_VALUE;

        Point secondIdealPosition = LabelTermas.calculatePointOnCircle(data.getCenter().getPoint(), data.getStart().getPoint(), (float)data.getRadius(), -2*Math.PI/3);
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

        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()].setDestination(firstDestination);
        }

        heap.insert(nodeLabels[startID]);
        nodeLabels[startID].setCost(0);
        notifyOriginProcessed(data.getStart());

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
                    if (successorLabel.getCost() != Float.MAX_VALUE)
                        heap.remove(successorLabel);
                    else
                        notifyNodeReached(successor);

                    int res = successorLabel.updateCostAndParent(minVertex.getCost() + (float) data.getCost(arc),
                             arc);

                    heap.insert(successorLabel);
                }
            }
        }

        if (!isDestinationMarked) {
            solution = new TermasSolution(data, Status.INFEASIBLE);
            return solution;
        }

        // for (Node node : graph.getNodes()) {
        //     nodeLabels[node.getId()].setDestination(secondDestination);
        // }

        notifyDestinationReached(firstDestination);
        ArrayList<Arc> shortestArcs = new ArrayList<>();
        LabelTermas goingBack = nodeLabels[firstDestinationID];
        // System.out.println("Finished, going back");
        while (goingBack.getParent() != null) {
            shortestArcs.add(goingBack.getParent());
            goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        }
        Collections.reverse(shortestArcs);
        solution = new TermasSolution(data, Status.OPTIMAL, new Path(graph, shortestArcs));
        return solution;
    }

}
