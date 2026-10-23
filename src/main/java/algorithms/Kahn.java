package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayDeque;

public class Kahn {
    public static int[] order(Graph g, Metrics metrics) {
        if (!g.directed()) throw new IllegalArgumentException("Topological order needs a directed graph");
        int n = g.size();
        int[] indegree = new int[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            indegree[v] = g.indegree(v);
            if (indegree[v] == 0) queue.add(v);
        }
        int[] order = new int[n];
        int length = 0;
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order[length++] = v;
            for (Edge e : g.outgoing(v)) {
                metrics.edgeScans++;
                if (--indegree[e.to()] == 0) queue.add(e.to());
            }
        }
        if (length != n) throw new IllegalArgumentException("Graph has a cycle");
        return order;
    }

    public static boolean valid(Graph g, int[] order) {
        if (order.length != g.size()) return false;
        int[] position = new int[g.size()];
        boolean[] seen = new boolean[g.size()];
        for (int i = 0; i < order.length; i++) {
            int v = order[i];
            if (v < 0 || v >= g.size() || seen[v]) return false;
            seen[v] = true;
            position[v] = i;
        }
        for (Edge e : g.edges())
            if (position[e.from()] >= position[e.to()]) return false;
        return true;
    }
}
