package org.insa.graphs.gui.simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;
import org.insa.graphs.algorithm.shortestpath.BellmanFordAlgorithm;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.gui.drawing.Drawing;
import org.insa.graphs.gui.drawing.components.BasicDrawing;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import org.insa.graphs.model.io.PathReader;

import java.util.concurrent.ThreadLocalRandom;

public class ShortestPathAlgorithmTest {

    /**
     * Create a new Drawing inside a JFrame an return it.
     * 
     * @return The created drawing.
     * 
     * @throws Exception if something wrong happens when creating the graph.
     */
    public static Drawing createDrawing() throws Exception {
        BasicDrawing basicDrawing = new BasicDrawing();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("BE Graphes - Launch");
                frame.setLayout(new BorderLayout());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                frame.setSize(new Dimension(800, 600));
                frame.setContentPane(basicDrawing);
                frame.validate();
            }
        });
        return basicDrawing;
    }

    public static void testAll(int origin, int destination, int road, Graph graph) {
        Node originNode = new Node(origin, null);
        Node destinationNode = new Node(destination, null);
        List<ArcInspector> listInspector = new ArcInspectorFactory().getAllFilters();
        ArcInspector aInspector = listInspector.get(road);
        ShortestPathData data = new ShortestPathData(graph, originNode, destinationNode, aInspector);
        
        BellmanFordAlgorithm bellmanford = new BellmanFordAlgorithm(data);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(data);
        AStarAlgorithm astar = new AStarAlgorithm(data);

        ShortestPathSolution bellmanfordSolution = bellmanford.run();
        System.out.println("- Bellman-Ford: (feasible: " + bellmanfordSolution.isFeasible() + ") " + bellmanfordSolution.getPath().getLength() + "m, " +  bellmanfordSolution.getPath().getMinimumTravelTime() + "s [solved in " + bellmanfordSolution.getSolvingTime() + "s]");
        ShortestPathSolution dijkstraSolution = dijkstra.run();
        System.out.println("- Dijkstra: (feasible: " + dijkstraSolution.isFeasible() + ") " + dijkstraSolution.getPath().getLength() + "m, " +  dijkstraSolution.getPath().getMinimumTravelTime() + "s [solved in " + dijkstraSolution.getSolvingTime() + "s]");
        ShortestPathSolution astarSolution = astar.run();
        System.out.println("- A*: (feasible: " + astarSolution.isFeasible() + ") " + astarSolution.getPath().getLength() + "m, " +  astarSolution.getPath().getMinimumTravelTime() + "s [solved in " + astarSolution.getSolvingTime() + "s]");

    }

    public static void execTests(Graph graph, int numberOfTests)
    {
        int numberOfNodes = graph.getNodes().size();
        for (int i = 0; i < numberOfTests; i++) {
            int originIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            int destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            while (destinationIndex == originIndex) {
                destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            }
            System.out.println("TEST[" + i + "/"+ numberOfTests +"]: " + originIndex + " -> " + destinationIndex);
            testAll(originIndex, destinationIndex, 0, graph);
        }
    }

    public static void main(String[] args) throws Exception {

        // Visit these directory to see the list of available files on Commetud.
        //final String mapName = "/mnt/commetud/3eme Annee MIC/Graphes-et-Algorithmes/Maps/insa.mapgr";
        final String carreMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/carre.mapgr";
        final String insaMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/insa.mapgr";
        final String toulouseMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/toulouse.mapgr";
        final String midipyreneesMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/toulouse.mapgr";
        //final String pathName = "/mnt/commetud/3eme Annee MIC/Graphes-et-Algorithmes/Paths/path_fr31insa_rangueil_r2.path";

        // Create a graph reader.
        final GraphReader carreGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(carreMapName))));
        final Graph carreGraph = carreGraphReader.read();
        carreGraphReader.close();

        final GraphReader insaGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(insaMapName))));
        final Graph insaGraph = insaGraphReader.read();
        insaGraphReader.close();

        final GraphReader toulouseGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(toulouseMapName))));
        final Graph toulouseGraph = toulouseGraphReader.read();
        toulouseGraphReader.close();

        final GraphReader midipyreneesGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(midipyreneesMapName))));
        final Graph midipyreneesGraph = midipyreneesGraphReader.read();
        midipyreneesGraphReader.close();

        // Create the drawing:
        //final Drawing drawing = createDrawing();

        execTests(carreGraph, 10);

        // TODO: Create a PathReader.
        final PathReader pathReader = null;

        // TODO: Read the path.
        final Path path = null;

    }

}
