package benchmark;

import graph.Graph;
import java.util.Random;

public class Generator {
    public static Graph generate(int n, int m, String kind) {
        Random random = new Random(42);
        boolean undirected = kind.equals("undirected");
        boolean dag = kind.equals("dag");
        Graph g = new Graph(n, !undirected);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        if (undirected || dag) {
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = perm[i]; perm[i] = perm[j]; perm[j] = temp;
            }
        }
        if (undirected) {
            for (int i = 1; i < n; i++) {
                int a = perm[random.nextInt(i)];
                g.addEdge(a, perm[i], 1 + random.nextInt(1000));
            }
        }
        while (g.edges().size() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            while (a == b) b = random.nextInt(n);
            if (dag && perm[a] > perm[b]) {
                int temp = a; a = b; b = temp;
            }
            g.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return g;
    }
}
