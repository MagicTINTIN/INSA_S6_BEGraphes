package org.insa.graphs.algorithm.shortestpath;

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

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    protected Label createLabel(Node n, Node d, Mode m, int maxSpeed) {
        return new Label(n);
    }

    @Override
    protected ShortestPathSolution doRun() {
        final ShortestPathData data = getInputData();
        final int originID = data.getOrigin().getId();
        final int destinationID = data.getDestination().getId();
        boolean isDestinationMarked = false;
        ShortestPathSolution solution = null;

        // initialising
        Graph graph = data.getGraph();
        int maxSpeed = data.getGraph().getGraphInformation().getMaximumSpeed();
        if (maxSpeed == GraphStatistics.NO_MAXIMUM_SPEED)
            maxSpeed = 142;
        final int nbNodes = graph.size();
        // System.out.println("Max speed used on the graph: " + maxSpeed + "km/h");
        Label nodeLabels[] = new Label[nbNodes];
        BinaryHeap<Label> heap = new BinaryHeap<>();
        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()] = createLabel(node, data.getDestination(), data.getMode(), maxSpeed);
        }

        heap.insert(nodeLabels[originID]);
        nodeLabels[originID].setCost(0);

        while (!heap.isEmpty() && !isDestinationMarked) {
            // System.out.println("Exploring new edge");
            Label minVertex = heap.deleteMin();

            nodeLabels[minVertex.getID()].mark();
            if (minVertex.getID() == destinationID) {
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

                Label successorLabel = nodeLabels[successor.getId()];

                if (!successorLabel.isMarked()) {
                    // System.out.println("Reaching node " + successor.getId());
                    if (successorLabel.getCost() != Float.MAX_VALUE)
                        heap.remove(successorLabel);
                    else
                        notifyNodeReached(successor);

                    int res = successorLabel.updateCostAndParent(minVertex.getCost() + (float) data.getCost(arc),
                             arc);

                    // Another version
                    // successorLabel.reach();
                    // float newDistance = minVertex.getCost() + (float) data.getCost(arc);
                    // if (!successorLabel.isMarked() && newDistance < successorLabel.getCost())
                    //     successorLabel.setCostAndParent(newDistance, arc);

                    // System.out.println("Updating vertex cost: " + res + ", accessing " +
                    // successor.getId()
                    // + " out of " + nbNodes);
                    heap.insert(successorLabel);
                    // System.out.println("Heap size : " + heap.size());
                    // try {
                    // // notifyNodeReached(successor);
                    // } catch (Exception e) {
                    // System.err.println("Error while removing/inserting node : " + e);
                    // }

                }
            }
        }

        if (!isDestinationMarked) {
            solution = new ShortestPathSolution(data, Status.INFEASIBLE);
            ;
            return solution;
        }

        ArrayList<Arc> shortestArcs = new ArrayList<>();
        Label goingBack = nodeLabels[destinationID];
        // System.out.println("Finished, going back");
        while (goingBack.getParent() != null) {
            shortestArcs.add(goingBack.getParent());
            goingBack = nodeLabels[goingBack.getParent().getOrigin().getId()];
        }
        Collections.reverse(shortestArcs);
        solution = new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, shortestArcs));
        return solution;
    }

}
