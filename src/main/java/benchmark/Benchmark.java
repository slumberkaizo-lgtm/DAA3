package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.ToLongFunction;

public class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    // Rows are ordered by size, sparse then dense, exactly as in the assignment.
    private static final long[][] REFERENCES = {
        {147970, 27, 396274}, {30363, 1, 252411},
        {468911, 137, 1271911}, {97589, 1, 723157},
        {1486139, 373, 3941828}, {309550, 1, 2381342},
        {4715515, 1188, 12551891}, {968517, 1, 7376612},
        {15012094, 3923, 40096539}, {3056681, 1, 23512087}
    };

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static long measure(PrintWriter csv, String name, String density, Graph g,
                                ToLongFunction<Metrics> algorithm) {
        double[] times = new double[5];
        long result = 0;
        Metrics first = null;
        for (int run = 0; run < 5; run++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            long current = algorithm.applyAsLong(metrics);
            times[run] = (System.nanoTime() - start) / 1_000_000.0;
            if (run == 0) { first = metrics; result = current; }
            else {
                require(result == current, "Result changed between runs: " + name);
                require(first.dsuOps() == metrics.dsuOps()
                        && first.edgeScans == metrics.edgeScans
                        && first.comparisons == metrics.comparisons
                        && first.maxDepth == metrics.maxDepth, "Counters changed: " + name);
            }
        }
        int m = g.edges().size();
        if (name.equals("Kosaraju")) require(first.edgeScans == 2L * m, "SCC scan count");
        if (name.equals("Kahn") || name.equals("DagShortestPath"))
            require(first.edgeScans == m, "DAG scan count");
        Arrays.sort(times);
        csv.printf(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
                name, density, g.size(), m, times[2], first.dsuOps(), first.edgeScans,
                first.comparisons, first.maxDepth, result);
        csv.flush();
        return result;
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("results"));
        // Replace the final CSV only after every graph passes its checks.
        Path temporary = Path.of("results/results.csv.tmp");
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(temporary))) {
            csv.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            int row = 0;
            for (int n : SIZES) {
                for (int density : new int[]{4, 20}) {
                    int m = n * density;
                    String label = density == 4 ? "sparse" : "dense";
                    Graph roads = Generator.generate(n, m, "undirected");
                    long kruskal = measure(csv, "Kruskal", label, roads,
                            metrics -> {
                                var tree = MST.kruskal(roads, metrics);
                                require(tree.edges().size() == n - 1 && tree.components() == 1, "Invalid Kruskal tree");
                                return tree.weight();
                            });
                    long prim = measure(csv, "Prim", label, roads,
                            metrics -> {
                                var tree = MST.prim(roads, metrics);
                                require(tree.edges().size() == n - 1 && tree.components() == 1, "Invalid Prim tree");
                                return tree.weight();
                            });
                    require(kruskal == prim, "MST algorithms disagree");
                    Graph directed = Generator.generate(n, m, "directed");
                    long scc = measure(csv, "Kosaraju", label, directed,
                            metrics -> Kosaraju.run(directed, metrics).count());
                    Graph dag = Generator.generate(n, m, "dag");
                    long ordered = measure(csv, "Kahn", label, dag, metrics -> {
                        int[] order = Kahn.order(dag, metrics);
                        require(Kahn.valid(dag, order), "Invalid topological order");
                        return order.length;
                    });
                    require(ordered == n, "Missing vertices in topological order");
                    long sum = measure(csv, "DagShortestPath", label, dag,
                            metrics -> DagShortestPath.run(dag, metrics).sum());
                    System.out.printf("%d / %d: MST=%d SCC=%d DAG=%d%n", n, m, kruskal, scc, sum);
                    long[] reference = REFERENCES[row++];
                    require(kruskal == reference[0] && scc == reference[1] && sum == reference[2],
                            "Reference mismatch at n=" + n + ", m=" + m);
                }
            }
        }
        Files.move(temporary, Path.of("results/results.csv"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("All 50 rows passed. Saved results/results.csv");
    }
}
