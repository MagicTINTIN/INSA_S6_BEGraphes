package org.insa.graphs.algorithm.termas;

import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Path;
import org.insa.graphs.algorithm.AbstractSolution;

public class TermasSolution extends AbstractSolution {

    // Optimal solution.
    private final Path path;

    /**
     * Create a new infeasible shortest-path solution for the given input and
     * status.
     * 
     * @param data Original input data for this solution.
     * @param status Status of the solution (UNKNOWN / INFEASIBLE).
     */
    public TermasSolution(TermasData data, Status status) {
        super(data, status);
        this.path = null;
    }

    /**
     * Create a new shortest-path solution.
     * 
     * @param data Original input data for this solution.
     * @param status Status of the solution (FEASIBLE / OPTIMAL).
     * @param path Path corresponding to the solution.
     */
    public TermasSolution(TermasData data, Status status, Path path) {
        super(data, status);
        this.path = path;
    }

    @Override
    public TermasData getInputData() {
        return (TermasData) super.getInputData();
    }

    /**
     * @return The path of this solution, if any.
     */
    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        String info = null;
        if (!isFeasible()) {
            info = String.format("No path found around node #%d starting by node #%d (between %f and %f)",
                    getInputData().getCenter().getId(), getInputData().getStart().getId(), getInputData().getMin() , getInputData().getMax());
        }
        else {
            double cost = 0;
            for (Arc arc: getPath().getArcs()) {
                cost += getInputData().getCost(arc);
            }
            info = String.format("Found a path found around node #%d starting by node #%d (between %f and %f)",
                getInputData().getCenter().getId(), getInputData().getStart().getId(), getInputData().getMin() , getInputData().getMax());
            if (getInputData().getMode() == Mode.LENGTH) {
                info = String.format("%s, %.4f kilometers", info, cost / 1000.0);
            }
            else {
                info = String.format("%s, %.4f minutes", info, cost / 60.0);
            }
        }
        info += " in " + (getSolvingTime().getNano()/1000) + " ms.";
        return info;
    }

}
