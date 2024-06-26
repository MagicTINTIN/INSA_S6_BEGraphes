package org.insa.graphs.algorithm.termas;

import org.insa.graphs.algorithm.AbstractAlgorithm;
import org.insa.graphs.model.Node;

public abstract class TermasAlgorithm extends AbstractAlgorithm<TermasObserver> {

    protected TermasAlgorithm(TermasData data) {
        super(data);
    }

    @Override
    public TermasSolution run() {
        return (TermasSolution) super.run();
    }

    @Override
    protected abstract TermasSolution doRun();

    @Override
    public TermasData getInputData() {
        return (TermasData) super.getInputData();
    }

    /**
     * Notify all observers that the origin has been processed.
     * 
     * @param node Origin.
     */
    public void notifyOriginProcessed(Node node) {
        for (TermasObserver obs: getObservers()) {
            obs.notifyOriginProcessed(node);
        }
    }

    /**
     * Notify all observers that a node has been reached for the first time.
     * 
     * @param node Node that has been reached.
     */
    public void notifyNodeReached(Node node) {
        for (TermasObserver obs: getObservers()) {
            obs.notifyNodeReached(node);
        }
    }

    /**
     * Notify all observers that a node has been marked, i.e. its final value has
     * been set.
     * 
     * @param node Node that has been marked.
     */
    public void notifyNodeMarked(Node node) {
        for (TermasObserver obs: getObservers()) {
            obs.notifyNodeMarked(node);
        }
    }

    /**
     * Notify all observers that the destination has been reached.
     * 
     * @param node Destination.
     */
    public void notifyDestinationReached(Node node) {
        for (TermasObserver obs: getObservers()) {
            obs.notifyDestinationReached(node);
        }
    }
}
