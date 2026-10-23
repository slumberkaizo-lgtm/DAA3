package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import java.util.Arrays;

public class Kosaraju {
    public record Result(int count, int[] component) { }

    public static Result run(Graph g, Metrics metrics) {
        if (!g.directed()) throw new IllegalArgumentException("SCC needs a directed graph");
        int n = g.size();
        boolean[] seen = new boolean[n];
        int[] stack = new int[n];
        int[] nextEdge = new int[n];
        int[] finish = new int[n];
        int finished = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            int top = 0;
            stack[top++] = start;
            seen[start] = true;
            metrics.depth(top);
            while (top > 0) {
                int v = stack[top - 1];
                if (nextEdge[v] == g.outgoing(v).size()) {
                    finish[finished++] = v;
                    top--;
                } else {
                    Edge edge = g.outgoing(v).get(nextEdge[v]++);
                    metrics.edgeScans++;
                    int to = edge.to();
                    if (!seen[to]) {
                        seen[to] = true;
                        stack[top++] = to;
                        metrics.depth(top);
                    }
                }
            }
        }
        Graph reverse = g.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;
        for (int i = finished - 1; i >= 0; i--) {
            int start = finish[i];
            if (component[start] != -1) continue;
            int top = 0;
            stack[top++] = start;
            component[start] = count;
            metrics.depth(top);
            while (top > 0) {
                int v = stack[--top];
                for (Edge e : reverse.outgoing(v)) {
                    metrics.edgeScans++;
                    if (component[e.to()] == -1) {
                        component[e.to()] = count;
                        stack[top++] = e.to();
                        metrics.depth(top);
                    }
                }
            }
            count++;
        }
        return new Result(count, component);
    }
}
