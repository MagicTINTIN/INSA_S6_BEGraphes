package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.algorithm.AbstractAlgorithm;
import org.insa.graphs.algorithm.AbstractInputData;
import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.RoadInformation;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected Label createLabel(Node n, Node d, Mode m, int maxSpeed)
    {
        int mSpeed = -1;
        if (m.equals(AbstractInputData.Mode.TIME))
            mSpeed = maxSpeed;
        return new LabelStar(n, d, mSpeed);
    }

}
