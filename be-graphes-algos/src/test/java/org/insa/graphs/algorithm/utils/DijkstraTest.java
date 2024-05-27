package org.insa.graphs.algorithm.utils;

import org.insa.graphs.algorithm.AbstractInputData;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.ShortestPathAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.algorithm.utils.PriorityQueue;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;
import org.insa.graphs.algorithm.shortestpath.BellmanFordAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathAlgorithm;

@RunWith(Parameterized.class)
public class DijkstraTest {

    private ShortestPathData inputData;
    private ShortestPathAlgorithm algorithm;
    private ShortestPathSolution solution;

    public static Graph read(String mapName) throws IOException {
        GraphReader reader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(mapName))));

        return reader.read();
    }

    public DijkstraTest(ShortestPathData inputData) {
        this.inputData = inputData;
    }

    @Parameterized.Parameters
    public static Collection<Object> data() throws IOException {
        Collection<Object> data = new ArrayList<>();

        final Graph map = read("/home/user/Documents/Scolaire/S6/graphes/INSA_S6_BEGraphes/Maps/insa.mapgr");

        // edge cases

        // classic
        data.add(new ShortestPathData(
                map,
                map.get(307),
                map.get(107),
                ArcInspectorFactory.getAllFilters().get(0)));

        // start is end
        data.add(new ShortestPathData(
                map,
                map.get(804),
                map.get(804),
                ArcInspectorFactory.getAllFilters().get(0)));

        // innacessible
        data.add(new ShortestPathData(
                map,
                map.get(866),
                map.get(804),
                ArcInspectorFactory.getAllFilters().get(0)));

        return data;
    }

    @Before
    public void init() {
        // use DijkstraAlgorithm
        this.algorithm = new DijkstraAlgorithm(this.inputData);
        this.solution = this.algorithm.run();
    }

    @Test
    public void testOrigin() {
        Assume.assumeTrue(solution.isFeasible());
        assertEquals(inputData.getOrigin(), solution.getInputData().getOrigin());
    }

    @Test
    public void testDestination() {
        Assume.assumeTrue(solution.isFeasible());
        assertEquals(inputData.getDestination(), solution.getInputData().getDestination());
    }

    @Test
    public void checkWithBellmanFord() {
        Assume.assumeTrue(this.inputData.getGraph().getNodes().size() <= 10000);
        BellmanFordAlgorithm AlgoB = new BellmanFordAlgorithm(this.inputData);
        ShortestPathSolution SolutionB = AlgoB.run();

        assertSame(SolutionB.getStatus(), this.solution.getStatus());

        if (!this.solution.isFeasible())
            return;

        assertEquals(SolutionB.getPath().getLength(), this.solution.getPath().getLength(), 0.1);
        assertSame(SolutionB.isFeasible(), this.solution.isFeasible());

        if (!this.solution.isFeasible()) {
            return;
        }

        // Algo to test
        Path path = this.solution.getPath();
        // Bellman-ford to compare
        Path pathB = SolutionB.getPath();

        assertSame(path.getArcs().size(), pathB.getArcs().size());

        for (int i = 0; i < path.getArcs().size(); i++) {
            assertSame(pathB.getArcs().get(i).getDestination(), path.getArcs().get(i).getDestination());
        }

    }
}