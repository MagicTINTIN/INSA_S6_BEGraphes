package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
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
        final int nbNodes = graph.size();
        Label nodeLabels[] = new Label[nbNodes];
        BinaryHeap<Label> heap = new BinaryHeap<>();
        for (Node node : graph.getNodes()) {
            nodeLabels[node.getId()] = new Label(node);
        }

        heap.insert(nodeLabels[originID]);
        nodeLabels[originID].setCost(0);

        while (!heap.isEmpty() && !isDestinationMarked)
        {
            
        }
        // for (int i = 0; i < nodeLabels.length; i++) {   
        // }
        return solution;
    }

}
