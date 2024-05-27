package org.insa.graphs.gui.simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledEditorKit.BoldAction;

import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;
import org.insa.graphs.algorithm.shortestpath.BellmanFordAlgorithm;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.gui.drawing.Drawing;
import org.insa.graphs.gui.drawing.components.BasicDrawing;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import org.insa.graphs.model.io.PathReader;

import java.util.concurrent.ThreadLocalRandom;
import java.text.SimpleDateFormat;
import java.text.Format;

public class ShortestPathAlgorithmRecursiveTestDijkstra {

    private static boolean identicalPaths(Path p1, Path p2, int min, int max) {
        if (p1 == null && p2 == null)
            return true;
        else if ((p1 == null && p2 != null) || (p1 != null && p2 == null) || p1.getArcs().size() != p2.getArcs().size())
            return false;
        for (int i = min; i < p1.getArcs().size() && (max == -1 || i < max); i++) {
            if (p1.getArcs().get(i).getDestination().getId() != p2.getArcs().get(i).getDestination().getId()) {
                System.out.println("Difference path: " + p1.getArcs().get(i).getDestination().getId() + ", "
                        + p2.getArcs().get(i).getDestination().getId());
                return false;
            }
        }
        return true;
    }

    private static boolean identicalPaths(Path p1, Path p2) {
        return identicalPaths(p1, p2, 0, -1);
    }

    public static void testDijkstraRecursivity(int road, Graph graph) {
        int numberOfNodes = graph.getNodes().size();
        int originIndex, destinationIndex;

        List<ArcInspector> listInspector = new ArcInspectorFactory().getAllFilters();
        ArcInspector aInspector = listInspector.get(road);

        ShortestPathSolution dijkstraSolution;
        DijkstraAlgorithm dijkstra;
        ShortestPathData data;
        Node originNode, destinationNode;

        // find a valid path to test the algotithm (and java is to stupid to understand that the loop will initialise in any case the solution)
        boolean validPath = false;
        originIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
        destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
        while (destinationIndex == originIndex) {
            destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
        }
        originNode = graph.getNodes().get(originIndex);
        destinationNode = graph.getNodes().get(destinationIndex);

        data = new ShortestPathData(graph, originNode, destinationNode, aInspector);

        // test the validity of the path
        dijkstra = new DijkstraAlgorithm(data);
        dijkstraSolution = dijkstra.run();

        if (dijkstraSolution.isFeasible())
            validPath = true;
        while (!validPath) {
            originIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            while (destinationIndex == originIndex) {
                destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            }
            originNode = graph.getNodes().get(originIndex);
            destinationNode = graph.getNodes().get(destinationIndex);

            data = new ShortestPathData(graph, originNode, destinationNode, aInspector);

            // test the validity of the path
            dijkstra = new DijkstraAlgorithm(data);
            dijkstraSolution = dijkstra.run();

            if (dijkstraSolution.isFeasible())
                validPath = true;
        }

        if (recursiveDijkstraCheck(road, graph, dijkstraSolution.getPath().getArcs()))
            System.out.println("APPROVED : Every subpath of the shortest path is a shortest path");
        else
            System.out.println("ERROR : a subpath of the shortest path is not a shortest path");
    }

    public static boolean recursiveDijkstraCheck(int road, Graph graph, List<Arc> arcs) {
        if (arcs.size() < 2)
            return true;

        int mid = arcs.size() / 2;

        // split the path into two subpaths
        List<Arc> subPath1 = arcs.subList(0, mid);
        List<Arc> subPath2 = arcs.subList(mid, arcs.size());

        Node origin1 = subPath1.get(0).getOrigin();
        Node destination1 = subPath1.get(subPath1.size() - 1).getDestination();

        Node origin2 = subPath2.get(0).getOrigin();
        Node destination2 = subPath2.get(subPath2.size() - 1).getDestination();

        // for each subpath
        ArcInspector aInspector = new ArcInspectorFactory().getAllFilters().get(road);

        ShortestPathData data1 = new ShortestPathData(graph, origin1, destination1, aInspector);
        ShortestPathData data2 = new ShortestPathData(graph, origin2, destination2, aInspector);

        DijkstraAlgorithm dijkstra1 = new DijkstraAlgorithm(data1);
        ShortestPathSolution solution1 = dijkstra1.run();
        DijkstraAlgorithm dijkstra2 = new DijkstraAlgorithm(data2);
        ShortestPathSolution solution2 = dijkstra2.run();

        // check if each subpath is the shortest path
        if (!solution1.isFeasible() || !solution2.isFeasible())
            return false;

        if (!identicalPaths(new Path(graph, subPath1), solution1.getPath()) || 
            !identicalPaths(new Path(graph, subPath2), solution2.getPath())) {
            return false;
        }

        // recursively check the subpaths
        return recursiveDijkstraCheck(road, graph, subPath1) && recursiveDijkstraCheck(road, graph, subPath2);
    }

    public static void main(String[] args) throws Exception {

        final String midipyreneesMapName = "Maps/midi-pyrenees.mapgr";

        System.out.println("\n\nLoading map...");
        final GraphReader midipyreneesGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(midipyreneesMapName))));
        Graph midipyreneesGraph = midipyreneesGraphReader.read();
        midipyreneesGraphReader.close();
        testDijkstraRecursivity(0, midipyreneesGraph);
        midipyreneesGraph = null;

        System.out.println("--- END OF THE TESTS ---");

    }

}
