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
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import org.insa.graphs.model.io.PathReader;

import java.util.concurrent.ThreadLocalRandom;
import java.text.SimpleDateFormat;
import java.text.Format;

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

    public static String testAll(int origin, int destination, int road, Graph graph) {
        String returnVal = "";
        Node originNode = graph.getNodes().get(origin);// new Node(origin, null);
        Node destinationNode = graph.getNodes().get(destination);// new Node(destination, );
        List<ArcInspector> listInspector = new ArcInspectorFactory().getAllFilters();
        ArcInspector aInspector = listInspector.get(road);
        ShortestPathData data = new ShortestPathData(graph, originNode, destinationNode, aInspector);

        BellmanFordAlgorithm bellmanford = new BellmanFordAlgorithm(data);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(data);
        AStarAlgorithm astar = new AStarAlgorithm(data);

        ShortestPathSolution bellmanfordSolution = bellmanford.run();
        if (bellmanfordSolution.isFeasible())
            returnVal += "1," + bellmanfordSolution.getPath().getLength() + ","
                    + bellmanfordSolution.getPath().getMinimumTravelTime() + ","
                    + bellmanfordSolution.getSolvingTime().toMillis() + ",";
        else
            returnVal += "0,-1,-1,"
                    + bellmanfordSolution.getSolvingTime().toMillis() + ",";
        System.out.println("- Bellman-Ford: solved in " + bellmanfordSolution.getSolvingTime().toMillis() + "ms");

        ShortestPathSolution dijkstraSolution = dijkstra.run();
        if (dijkstraSolution.isFeasible())
            returnVal += "1," + dijkstraSolution.getPath().getLength() + ","
                    + dijkstraSolution.getPath().getMinimumTravelTime() + ","
                    + dijkstraSolution.getSolvingTime().toMillis() + ",";
        else
            returnVal += "0,-1,-1,"
                    + dijkstraSolution.getSolvingTime().toMillis() + ",";
        System.out.println("- Dijkstra: solved in " + dijkstraSolution.getSolvingTime().toMillis() + "ms");

        ShortestPathSolution astarSolution = astar.run();
        if (astarSolution.isFeasible())
            returnVal += "1," + astarSolution.getPath().getLength() + ","
                    + astarSolution.getPath().getMinimumTravelTime() + ","
                    + astarSolution.getSolvingTime().toMillis() + "\n";
        else
            returnVal += "0,-1,-1,"
                    + astarSolution.getSolvingTime().toMillis() + "\n";
        System.out.println("- A*: solved in " + astarSolution.getSolvingTime().toMillis() + "ms");
        return returnVal;
    }

    public static void execTests(Graph graph, int numberOfTests, String testName, Boolean all) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String outputResNameFile = "test" + testName + "_" + formatter.format(new Date()) + ".csv";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFile, true));
            if (all)
                writer.append("feasibleBellman,lengthBellman,timeBellman,solvingTimeBellman,feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar\n");
            else
                writer.append("feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar\n");

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!\nHead not written.");
        }

        int numberOfNodes = graph.getNodes().size();
        for (int i = 0; i < numberOfTests; i++) {
            int originIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            int destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            while (destinationIndex == originIndex) {
                destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes + 1);
            }
            System.out.println(testName + ": TEST[" + i + "/" + numberOfTests + "]: " + originIndex + " -> " + destinationIndex);

            String res = testAll(originIndex, destinationIndex, 0, graph);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFile, true));
                writer.append(res);

                writer.close();
            } catch (Exception e) {
                System.err.println("ERROR WITH FILE WRITING!");
                System.out.println(res);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        // Visit these directory to see the list of available files on Commetud.
        // final String mapName = "/mnt/commetud/3eme Annee
        // MIC/Graphes-et-Algorithmes/Maps/insa.mapgr";
        
        // final String carreMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/carre.mapgr";
        // final String insaMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/insa.mapgr";
        // final String toulouseMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/toulouse.mapgr";
        // final String midipyreneesMapName = "/home/utilisateur/INSA/S6/graphes/INSA_S6_BEGraphes/Maps/toulouse.mapgr";
        
        final String carreMapName = "Maps/carre.mapgr";
        final String insaMapName = "Maps/insa.mapgr";
        final String toulouseMapName = "Maps/toulouse.mapgr";
        final String midipyreneesMapName = "Maps/toulouse.mapgr";
        
        // final String pathName = "/mnt/commetud/3eme Annee
        // MIC/Graphes-et-Algorithmes/Paths/path_fr31insa_rangueil_r2.path";

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
        // final Drawing drawing = createDrawing();

        execTests(insaGraph, 10, "insa", true);

        // TODO: Create a PathReader.
        final PathReader pathReader = null;

        // TODO: Read the path.
        final Path path = null;

    }

}
