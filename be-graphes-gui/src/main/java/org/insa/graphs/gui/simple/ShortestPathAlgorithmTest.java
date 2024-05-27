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

    protected static String globalAll = "map,totalNodes,feasibleBellman,lengthBellman,timeBellman,solvingTimeBellman,feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar,mode,arcs,valid\n";
    protected static String globalDijastar = "map,totalNodes,feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar,mode,arcs,valid\n";

    private static boolean identicalPaths(Path p1, Path p2) {
        if (p1 == null && p2 == null)
            return true;
        else if ((p1 == null && p2 != null) || (p1 != null && p2 == null) || p1.getArcs().size() != p2.getArcs().size())
            return false;
        for (int i = 0; i < p1.getArcs().size(); i++) {
            if (p1.getArcs().get(i).getDestination().getId() != p2.getArcs().get(i).getDestination().getId()) {
                System.out.println("Difference path: " + p1.getArcs().get(i).getDestination().getId() + ", "
                        + p2.getArcs().get(i).getDestination().getId());
                return false;
            }
        }
        return true;
    }

    private static boolean sameResults(int mode, ShortestPathSolution s1, ShortestPathSolution s2, String note) {
        if (s1.getPath() == null && s2.getPath() == null)
            return true;
        else if ((s1.getPath() == null && s2.getPath() != null) || (s1.getPath() != null && s2.getPath() == null)) {
            if (note != null)
                System.err.println(note + " existance !");
            return false;
        }
        if (mode < 3) {
            if (note != null && s1.getPath().getLength() != s2.getPath().getLength())
                System.err.println(note + " length ! Diff: "
                        + Math.abs(s1.getPath().getLength() - s2.getPath().getLength()) + "m");
            return s1.getPath().getLength() == s2.getPath().getLength();
        } else {
            if (note != null && s1.getPath().getMinimumTravelTime() != s2.getPath().getMinimumTravelTime())
                System.err.println(note + " time ! Diff: "
                        + Math.abs(s1.getPath().getMinimumTravelTime() - s2.getPath().getMinimumTravelTime()) + "s");
            return s1.getPath().getMinimumTravelTime() == s2.getPath().getMinimumTravelTime();
        }
    }

    private static boolean sameResults(int mode, ShortestPathSolution s1, ShortestPathSolution s2) {
        return sameResults(mode, s1, s2, null);
    }

    public static class StringAndRes {
        public ShortestPathSolution solution;
        public String string;
        public boolean identic;
        public boolean valid;

        public StringAndRes(ShortestPathSolution sol, String s, boolean i, boolean v) {
            this.solution = sol;
            this.string = s;
            this.identic = i;
            this.valid = v;
        }
    }

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

    public static StringAndRes testAll(int origin, int destination, int road, Graph graph, int silent) {
        String returnStrVal = "";
        Node originNode = graph.getNodes().get(origin);// new Node(origin, null);
        Node destinationNode = graph.getNodes().get(destination);// new Node(destination, );
        List<ArcInspector> listInspector = new ArcInspectorFactory().getAllFilters();
        ArcInspector aInspector = listInspector.get(road);
        ShortestPathData data = new ShortestPathData(graph, originNode, destinationNode, aInspector);

        BellmanFordAlgorithm bellmanford = new BellmanFordAlgorithm(data);

        ShortestPathSolution bellmanfordSolution = bellmanford.run();
        if (bellmanfordSolution.isFeasible())
            returnStrVal += "1," + bellmanfordSolution.getPath().getLength() + ","
                    + bellmanfordSolution.getPath().getMinimumTravelTime() + ","
                    + bellmanfordSolution.getSolvingTime().toNanos() + ",";
        else
            returnStrVal += "0,-1,-1,"
                    + bellmanfordSolution.getSolvingTime().toNanos() + ",";
        if (silent <= 0)
            System.out.println("- Bellman-Ford: solved in " + bellmanfordSolution.getSolvingTime().toNanos() + "ns");

        StringAndRes res = testDijAstar(origin, destination, road, graph, silent);
        globalAll += returnStrVal + res.string;
        return new StringAndRes(bellmanfordSolution, returnStrVal + res.string,
                res.identic && identicalPaths(bellmanfordSolution.getPath(), res.solution.getPath()),
                res.valid && sameResults(road, bellmanfordSolution, res.solution, "ERROR belldij"));
    }

    public static StringAndRes testDijAstar(int origin, int destination, int road, Graph graph, int silent) {
        String returnStrVal = "";
        Node originNode = graph.getNodes().get(origin);// new Node(origin, null);
        Node destinationNode = graph.getNodes().get(destination);// new Node(destination, );
        List<ArcInspector> listInspector = new ArcInspectorFactory().getAllFilters();
        ArcInspector aInspector = listInspector.get(road);
        ShortestPathData data = new ShortestPathData(graph, originNode, destinationNode, aInspector);

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(data);
        AStarAlgorithm astar = new AStarAlgorithm(data);

        ShortestPathSolution dijkstraSolution = dijkstra.run();
        if (dijkstraSolution.isFeasible())
            returnStrVal += "1," + dijkstraSolution.getPath().getLength() + ","
                    + dijkstraSolution.getPath().getMinimumTravelTime() + ","
                    + dijkstraSolution.getSolvingTime().toNanos() + ",";
        else
            returnStrVal += "0,-1,-1,"
                    + dijkstraSolution.getSolvingTime().toNanos() + ",";
        if (silent <= 0)
            System.out.println("- Dijkstra: solved in " + dijkstraSolution.getSolvingTime().toNanos() + "ns");

        ShortestPathSolution astarSolution = astar.run();
        if (astarSolution.isFeasible())
            returnStrVal += "1," + astarSolution.getPath().getLength() + ","
                    + astarSolution.getPath().getMinimumTravelTime() + ","
                    + astarSolution.getSolvingTime().toNanos() + "," + road + ",";
        else
            returnStrVal += "0,-1,-1,"
                    + astarSolution.getSolvingTime().toNanos() + "," + road + ",";
        if (silent <= 0)
            System.out.println("- A*: solved in " + astarSolution.getSolvingTime().toNanos() + "ns");
        
        if (dijkstraSolution.isFeasible() && dijkstraSolution.getPath() != null && dijkstraSolution.getPath().getArcs() != null)
            returnStrVal += ((int)dijkstraSolution.getPath().getArcs().size()) + ",";
        else
            returnStrVal += "0,";
        
        globalDijastar += returnStrVal;
        return new StringAndRes(dijkstraSolution, returnStrVal,
                identicalPaths(dijkstraSolution.getPath(), astarSolution.getPath()),
                sameResults(road, dijkstraSolution, astarSolution, "ERROR dijastar"));
    }

    public static void execTests(Graph graph, int numberOfTests, String testName, boolean shortest, boolean all,
            int silent) {
        int mode = 0;
        if (!shortest)
            mode = 3;
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String outputResNameFile = "test" + testName + "_" + formatter.format(new Date()) + ".csv";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFile, true));
            if (all)
                writer.append(
                        "map,totalNodes,feasibleBellman,lengthBellman,timeBellman,solvingTimeBellman,feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar,mode,arcs,valid\n");
            else
                writer.append(
                        "map,totalNodes,feasibleDijkstra,lengthDijkstra,timeDijkstra,solvingTimeDijkstra,feasibleAStar,lengthAStar,timeAStar,solvingTimeAStar,mode,arcs,valid\n");

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!\nHead not written.");
        }

        int numberOfNodes = graph.getNodes().size();
        int numberOfSuccesses = 0;
        int numberOfIdentics = 0;
        for (int i = 0; i < numberOfTests; i++) {
            int originIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            int destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            while (destinationIndex == originIndex) {
                destinationIndex = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
            }
            if (silent <= 2)
                System.out.println(
                        testName + ": TEST[" + i + "/" + numberOfTests + "]: " + originIndex + " -> "
                                + destinationIndex);
            globalDijastar += testName + "," + numberOfNodes + ",";
            if (all)
                globalAll += testName + "," + numberOfNodes + ",";
            StringAndRes res;
            if (all)
                res = testAll(originIndex, destinationIndex, mode, graph, silent);
            else
                res = testDijAstar(originIndex, destinationIndex, mode, graph, silent);

            if (res.identic)
                numberOfIdentics++;

            String validStr = "1\n";
            if (res.valid)
                numberOfSuccesses++;
            else {
                validStr = "0\n";
                System.err.println("INVALID SOLUTION : " + testName + "(" + mode + ") => " + originIndex + " -> "
                        + destinationIndex);
            }

            globalDijastar += validStr;
            if (all)
                globalAll += validStr;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFile, true));
                writer.append(testName + "," + numberOfNodes + "," + res.string + validStr);
                writer.close();
            } catch (Exception e) {
                System.err.println("ERROR WITH FILE WRITING!");
                System.out.println(testName + "," + numberOfNodes + "," + res.string + validStr);
            }
        }
        if (silent <= 3)
            System.out.println(testName + ": Valid results > " + numberOfSuccesses + "/" + numberOfTests);
        if (silent <= 1)
            System.out.println(testName + ": Identic results > " + numberOfIdentics + "/" + numberOfTests);
    }

    public static void main(String[] args) throws Exception {

        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String outputResNameFileAll = "globalTestAll" + "_" + formatter.format(new Date()) + ".csv";
        String outputResNameFileDijastar = "globalTestDijastra" + "_" + formatter.format(new Date()) + ".csv";

        final String carreMapName = "Maps/carre.mapgr";
        final String insaMapName = "Maps/insa.mapgr";
        final String toulouseMapName = "Maps/toulouse.mapgr";
        final String hautegaronneMapName = "Maps/haute-garonne.mapgr";
        final String midipyreneesMapName = "Maps/midi-pyrenees.mapgr";
        final String franceMapName = "Maps/france.mapgr";

        int silence = 1;
        System.out.println("\n\nLoading map...");
        final GraphReader carreGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(carreMapName))));
        Graph carreGraph = carreGraphReader.read();
        carreGraphReader.close();
        execTests(carreGraph, 60, "carre", true, true, silence);
        execTests(carreGraph, 20, "carre", false, true, silence);
        carreGraph = null;

        System.out.println("\n\nLoading map...");
        final GraphReader insaGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(insaMapName))));
        Graph insaGraph = insaGraphReader.read();
        insaGraphReader.close();
        execTests(insaGraph, 60, "insa", true, true, silence);
        execTests(insaGraph, 20, "insa", false, true, silence);
        insaGraph = null;

        System.out.println("\n\nLoading map...");
        final GraphReader toulouseGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(toulouseMapName))));
        Graph toulouseGraph = toulouseGraphReader.read();
        toulouseGraphReader.close();
        execTests(toulouseGraph, 50, "toulouse", true, true, silence);
        execTests(toulouseGraph, 150, "toulouse", true, false, silence);
        execTests(toulouseGraph, 5, "toulouse", false, true, silence);
        execTests(toulouseGraph, 50, "toulouse", false, false, silence);
        toulouseGraph = null;

        System.out.println("\n\nLoading map...");
        final GraphReader hautegaronneGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(hautegaronneMapName))));
        Graph hautegaronneGraph = hautegaronneGraphReader.read();
        hautegaronneGraphReader.close();
        execTests(hautegaronneGraph, 130, "hg", true, false, silence);
        execTests(hautegaronneGraph, 35, "hg", false, false, silence);
        hautegaronneGraph = null;

        System.out.println("\n\nLoading map...");
        final GraphReader midipyreneesGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(midipyreneesMapName))));
        Graph midipyreneesGraph = midipyreneesGraphReader.read();
        midipyreneesGraphReader.close();
        execTests(midipyreneesGraph, 90, "mp", true, false, silence);
        execTests(midipyreneesGraph, 20, "mp", false, false, silence);
        midipyreneesGraph = null;


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("save1" + outputResNameFileAll, true));
            writer.append(globalAll);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalAll);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("save1" + outputResNameFileDijastar, true));
            writer.append(globalDijastar);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalDijastar);
        }

        System.out.println("\n\nLoading map...");
        final GraphReader franceGraphReader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream(franceMapName))));
        Graph franceGraph = franceGraphReader.read();
        franceGraphReader.close();
        execTests(franceGraph, 30, "fr", true, false, silence);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("save2" + outputResNameFileAll, true));
            writer.append(globalAll);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalAll);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("save2" + outputResNameFileDijastar, true));
            writer.append(globalDijastar);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalDijastar);
        }

        execTests(franceGraph, 2, "fr", false, false, silence);
        franceGraph = null;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFileAll, true));
            writer.append(globalAll);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalAll);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputResNameFileDijastar, true));
            writer.append(globalDijastar);

            writer.close();
        } catch (Exception e) {
            System.err.println("ERROR WITH FILE WRITING!");
            System.out.println(globalDijastar);
        }

        System.out.println("--- END OF THE TESTS ---");
    }

}
