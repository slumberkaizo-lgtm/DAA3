package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.nio.file.Path;
import java.util.Arrays;

public class Demo {
    public static void main(String[] args) throws Exception {
        Graph roads = Graph.read(Path.of("data/disconnected.txt"));
        System.out.println("Kruskal: " + MST.kruskal(roads, new Metrics()));
        System.out.println("Prim:    " + MST.prim(roads, new Metrics()));
        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        System.out.println("SCC count: " + Kosaraju.run(cyclic, new Metrics()).count());
        try { Kahn.order(cyclic, new Metrics()); }
        catch (IllegalArgumentException e) { System.out.println("Kahn: " + e.getMessage()); }
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        System.out.println("Order: " + Arrays.toString(Kahn.order(dag, new Metrics())));
        var paths = DagShortestPath.run(dag, new Metrics());
        System.out.println("Distances: " + Arrays.toString(paths.distance()));
        System.out.println("Path to 5: " + paths.pathTo(5));
    }
}
