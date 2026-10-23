package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DagShortestPath {
    public record Result(long[] distance, int[] parent) {
        public long sum() { return Arrays.stream(distance).sum(); }

        public List<Integer> pathTo(int v) {
            List<Integer> path = new ArrayList<>();
            for (int current = v; current != -1; current = parent[current]) path.add(current);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        // Kahn is part of the running time, but its scans have their own metric.
        int[] order = Kahn.order(g, new Metrics());
        return relax(g, order, metrics);
    }

    private static Result relax(Graph g, int[] order, Metrics metrics) {
        long[] distance = new long[g.size()];
        int[] parent = new int[g.size()];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        for (int v = 0; v < g.size(); v++)
            if (g.indegree(v) == 0) distance[v] = 0;
        for (int v : order) {
            for (Edge e : g.outgoing(v)) {
                metrics.edgeScans++;
                long candidate = distance[v] + e.weight();
                if (candidate < distance[e.to()]) {
                    distance[e.to()] = candidate;
                    parent[e.to()] = v;
                }
            }
        }
        return new Result(distance, parent);
    }
}
