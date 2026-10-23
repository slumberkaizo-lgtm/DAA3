package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MST {
    public record Result(long weight, List<Edge> edges, int components) { }

    private static void check(Graph g) {
        if (g.directed()) throw new IllegalArgumentException("MST needs an undirected graph");
    }

    public static Result kruskal(Graph g, Metrics metrics) {
        check(g);
        List<Edge> sorted = new ArrayList<>(g.edges());
        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(g.size(), metrics);
        List<Edge> tree = new ArrayList<>();
        long weight = 0;
        for (Edge e : sorted) {
            if (dsu.components() <= 1) break;
            metrics.edgeScans++;
            if (dsu.union(e.from(), e.to())) {
                tree.add(e);
                weight += e.weight();
            }
        }
        return new Result(weight, tree, dsu.components());
    }

    public static Result prim(Graph g, Metrics metrics) {
        check(g);
        boolean[] visited = new boolean[g.size()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> tree = new ArrayList<>();
        long weight = 0;
        int components = 0;
        // Starting again at each unvisited vertex also handles a forest.
        for (int start = 0; start < g.size(); start++) {
            if (visited[start]) continue;
            components++;
            queue.add(new Edge(-1, start, 0));
            while (!queue.isEmpty()) {
                Edge best = queue.remove();
                int v = best.to();
                if (visited[v]) continue;
                visited[v] = true;
                if (best.from() != -1) {
                    tree.add(best);
                    weight += best.weight();
                }
                for (Edge e : g.outgoing(v)) {
                    metrics.edgeScans++;
                    if (!visited[e.to()]) queue.add(e);
                }
            }
        }
        return new Result(weight, tree, components);
    }
}
