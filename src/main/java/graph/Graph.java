package graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Graph {
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] indegree;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("Negative vertex count");
        this.directed = directed;
        adjacency = new ArrayList<>(n);
        indegree = new int[n];
        for (int i = 0; i < n; i++) adjacency.add(new ArrayList<>());
    }

    public int size() { return adjacency.size(); }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return edges; }
    public List<Edge> outgoing(int v) { return adjacency.get(v); }
    public int indegree(int v) { return indegree[v]; }

    public void addEdge(int u, int v, int weight) {
        if (u < 0 || u >= size() || v < 0 || v >= size())
            throw new IllegalArgumentException("Vertex outside 0..n-1");
        Edge edge = new Edge(u, v, weight);
        edges.add(edge);
        adjacency.get(u).add(edge);
        indegree[v]++;
        if (!directed) {
            adjacency.get(v).add(new Edge(v, u, weight));
            indegree[u]++;
        }
    }

    public Graph reversed() {
        Graph reverse = new Graph(size(), directed);
        for (Edge e : edges) reverse.addEdge(e.to(), e.from(), e.weight());
        return reverse;
    }

    public static Graph read(Path path) throws IOException {
        try (Scanner input = new Scanner(path)) {
            int n = input.nextInt();
            int m = input.nextInt();
            String kind = input.next();
            if (!kind.equals("directed") && !kind.equals("undirected"))
                throw new IllegalArgumentException("Expected directed or undirected");
            if (m < 0) throw new IllegalArgumentException("Negative edge count");
            Graph graph = new Graph(n, kind.equals("directed"));
            for (int i = 0; i < m; i++)
                graph.addEdge(input.nextInt(), input.nextInt(), input.nextInt());
            if (input.hasNext()) throw new IllegalArgumentException("Extra input after m edges");
            return graph;
        }
    }
}
