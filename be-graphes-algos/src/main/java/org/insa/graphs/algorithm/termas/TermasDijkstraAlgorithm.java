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

public class TermasDijkstraAlgorithm extends TermasAlgorithm {
    private float minRadius;
    private float maxRadius;

    public TermasDijkstraAlgorithm(TermasData data) {
        super(data);
    }

    public TermasDijkstraAlgorithm(TermasData data, float minRadius, float maxRadius) {
        super(data);
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    protected LabelTermas createLabel(Node n, Node d, Mode m, int maxSpeed) {
        return new LabelTermas(n);
    }

    @Override
    protected TermasSolution doRun() {
        final TermasData data = getInputData();
        final int centerID = data.getOrigin().getId();
        final int destinationID = data.getDestination().getId();
        boolean isDestinationMarked = false;
        TermasSolution solution = null;

        // initialising
        Graph graph = data.getGraph();
        int maxSpeed = data.getGraph().getGraphInformation().getMaximumSpeed();
        if (maxSpeed == GraphStatistics.NO_MAXIMUM_SPEED || true)
            maxSpeed = 20;//142;
        final int nbNodes = graph.size();
        LabelTermas nodeLabels[] = new LabelTermas[nbNodes];
        BinaryHeap<LabelTermas> heap = new BinaryHeap<>();
        // for (Node node : graph.getNodes()) {
        //     nodeLabels[node.getId()] = createLabel(node, data.getDestination(), data.getMode(), maxSpeed);
        // }

        nodeLabels[centerID] = createLabel(data.getOrigin(), data.getDestination(), data.getMode(), maxSpeed);
        heap.insert(nodeLabels[centerID]);
        nodeLabels[centerID].setCost(0);
        notifyOriginProcessed(data.getOrigin());

        while (!heap.isEmpty() && !isDestinationMarked) {
            LabelTermas minVertex = heap.deleteMin();

            nodeLabels[minVertex.getID()].mark();
            if (minVertex.getID() == destinationID) {
                isDestinationMarked = true;
                break;
            }
            Node minNode = minVertex.getCurrentVertex();
            notifyNodeMarked(minNode);

            for (Arc arc : minNode.getSuccessors()) {
                Node successor = arc.getDestination();

                if (!data.isAllowed(arc) || !) {
                    continue;
                }

                if (nodeLabels[successor.getId()] == null)
                    nodeLabels[successor.getId()] = createLabel(graph.getNodes().get(successor.getId()), data.getDestination(), data.getMode(), maxSpeed);
                    LabelTermas successorLabel = nodeLabels[successor.getId()];

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

        notifyDestinationReached(data.getDestination());
        ArrayList<Arc> shortestArcs = new ArrayList<>();
        LabelTermas goingBack = nodeLabels[destinationID];
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
