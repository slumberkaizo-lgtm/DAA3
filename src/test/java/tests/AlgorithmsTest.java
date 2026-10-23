package tests;

import algorithms.*;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {
    @Test void dsuAgainstSimpleLabels() {
        int n = 80;
        int[] label = new int[n];
        for (int i = 0; i < n; i++) label[i] = i;
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(n, metrics);
        Random random = new Random(9);
        for (int step = 0; step < 500; step++) {
            int a = random.nextInt(n), b = random.nextInt(n);
            int old = label[b], replacement = label[a];
            assertEquals(old != replacement, dsu.union(a, b));
            for (int i = 0; i < n; i++) if (label[i] == old) label[i] = replacement;
            Set<Integer> sets = new HashSet<>();
            for (int i = 0; i < n; i++) {
                sets.add(label[i]);
                for (int j = 0; j < n; j++)
                    assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j));
            }
            assertEquals(sets.size(), dsu.components());
        }
        assertEquals(n - dsu.components(), metrics.unions);
        dsu.makeSet(3);
        assertEquals(3, dsu.components());
        assertNotEquals(dsu.find(0), dsu.find(1));
    }

    @Test void mstAgreementOn100ConnectedGraphs() {
        Random random = new Random(12);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(50);
            Graph g = new Graph(n, false);
            for (int v = 1; v < n; v++) g.addEdge(v, random.nextInt(v), random.nextInt(101) - 50);
            for (int i = 0; i < 5 * n; i++)
                g.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(101) - 50);
            var a = MST.kruskal(g, new Metrics());
            var b = MST.prim(g, new Metrics());
            assertEquals(a.weight(), b.weight());
            assertEquals(n - 1, a.edges().size());
            assertEquals(n - 1, b.edges().size());
            assertEquals(1, a.components());
            assertEquals(1, b.components());
        }
    }

    @Test void mstAgainstExhaustiveSubsets() {
        Random random = new Random(5);
        for (int trial = 0; trial < 30; trial++) {
            Graph g = new Graph(5, false);
            for (int v = 1; v < 5; v++) g.addEdge(v - 1, v, random.nextInt(21) - 10);
            for (int i = 0; i < 5; i++) g.addEdge(random.nextInt(5), random.nextInt(5), random.nextInt(21) - 10);
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << g.edges().size()); mask++) {
                if (Integer.bitCount(mask) != 4) continue;
                // Independent reachability check for each candidate tree.
                Graph candidate = new Graph(5, false);
                long weight = 0;
                for (int i = 0; i < g.edges().size(); i++) if ((mask & (1 << i)) != 0) {
                    Edge e = g.edges().get(i);
                    candidate.addEdge(e.from(), e.to(), e.weight());
                    weight += e.weight();
                }
                boolean[] reachable = bfs(candidate, 0);
                boolean connected = true;
                for (boolean reached : reachable) connected &= reached;
                if (connected) best = Math.min(best, weight);
            }
            assertEquals(best, MST.kruskal(g, new Metrics()).weight());
            assertEquals(best, MST.prim(g, new Metrics()).weight());
        }
    }

    private static boolean[] bfs(Graph g, int start) {
        boolean[] seen = new boolean[g.size()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        seen[start] = true;
        queue.add(start);
        while (!queue.isEmpty()) {
            for (Edge e : g.outgoing(queue.remove())) {
                if (!seen[e.to()]) { seen[e.to()] = true; queue.add(e.to()); }
            }
        }
        return seen;
    }

    @Test void sccAgainstReachability() {
        Random random = new Random(19);
        for (int trial = 0; trial < 40; trial++) {
            int n = trial == 0 ? 200 : 1 + random.nextInt(50);
            Graph g = new Graph(n, true);
            for (int i = 0; i < 3 * n; i++)
                g.addEdge(random.nextInt(n), random.nextInt(n), 1);
            boolean[][] reach = new boolean[n][];
            for (int i = 0; i < n; i++) reach[i] = bfs(g, i);
            Metrics metrics = new Metrics();
            var result = Kosaraju.run(g, metrics);
            Set<Integer> components = new HashSet<>();
            for (int a = 0; a < n; a++) {
                components.add(result.component()[a]);
                for (int b = 0; b < n; b++)
                    assertEquals(reach[a][b] && reach[b][a], result.component()[a] == result.component()[b]);
            }
            assertEquals(components.size(), result.count());
            assertEquals(2L * g.edges().size(), metrics.edgeScans);
        }
    }

    @Test void dagAgainstBellmanFordIncludingNegativeWeights() {
        Random random = new Random(33);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(40);
            List<Integer> perm = new ArrayList<>();
            for (int i = 0; i < n; i++) perm.add(i);
            Collections.shuffle(perm, random);
            Graph g = new Graph(n, true);
            for (int a = 0; a < n; a++) for (int b = a + 1; b < n; b++)
                if (random.nextInt(5) == 0) g.addEdge(perm.get(a), perm.get(b), random.nextInt(41) - 20);
            long[] expected = new long[n];
            Arrays.fill(expected, Long.MAX_VALUE);
            for (int v = 0; v < n; v++) if (g.indegree(v) == 0) expected[v] = 0;
            for (int pass = 1; pass < n; pass++) {
                for (Edge e : g.edges()) {
                    if (expected[e.from()] != Long.MAX_VALUE)
                        expected[e.to()] = Math.min(expected[e.to()], expected[e.from()] + e.weight());
                }
            }
            Metrics metrics = new Metrics();
            var actual = DagShortestPath.run(g, metrics);
            assertArrayEquals(expected, actual.distance());
            assertEquals(g.edges().size(), metrics.edgeScans);
            assertTrue(Kahn.valid(g, Kahn.order(g, new Metrics())));
            for (int v = 0; v < n; v++) {
                List<Integer> path = actual.pathTo(v);
                assertEquals(v, path.get(path.size() - 1));
                assertEquals(0, g.indegree(path.get(0)));
                long sum = 0;
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i - 1), to = path.get(i);
                    Edge edge = g.outgoing(from).stream().filter(e -> e.to() == to).findFirst().orElseThrow();
                    sum += edge.weight();
                }
                assertEquals(expected[v], sum);
            }
        }
    }

    @Test void inputFilesAndCornerCases() throws Exception {
        Graph forest = Graph.read(Path.of("data/disconnected.txt"));
        for (var tree : List.of(MST.kruskal(forest, new Metrics()), MST.prim(forest, new Metrics()))) {
            assertEquals(8, tree.weight());
            assertEquals(3, tree.components());
            assertEquals(3, tree.edges().size());
        }
        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        assertEquals(2, Kosaraju.run(cyclic, new Metrics()).count());
        assertThrows(IllegalArgumentException.class, () -> Kahn.order(cyclic, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(cyclic, new Metrics()));
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        assertArrayEquals(new long[]{0, 0, 2, -3, 3, -1}, DagShortestPath.run(dag, new Metrics()).distance());
        assertEquals(List.of(1, 2, 3, 5), DagShortestPath.run(dag, new Metrics()).pathTo(5));
        assertFalse(Kahn.valid(dag, new int[]{0, 0, 1, 2, 3, 4}));
        Graph loop = new Graph(2, true);
        loop.addEdge(0, 0, 1);
        loop.addEdge(0, 0, 2);
        assertEquals(2, Kosaraju.run(loop, new Metrics()).count());
        assertThrows(IllegalArgumentException.class, () -> Kahn.order(loop, new Metrics()));
        Graph empty = new Graph(0, true);
        assertEquals(0, Kosaraju.run(empty, new Metrics()).count());
        assertEquals(0, Kahn.order(empty, new Metrics()).length);
        assertEquals(0, DagShortestPath.run(empty, new Metrics()).sum());
        Graph noRoads = new Graph(0, false);
        assertEquals(0, MST.kruskal(noRoads, new Metrics()).components());
        assertEquals(0, MST.prim(noRoads, new Metrics()).weight());
    }

    @Test void longDistancesDoNotOverflowInt() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, Integer.MAX_VALUE);
        g.addEdge(1, 2, Integer.MAX_VALUE);
        g.addEdge(2, 3, Integer.MAX_VALUE);
        assertEquals(3L * Integer.MAX_VALUE, DagShortestPath.run(g, new Metrics()).distance()[3]);
    }

    @Test void iterativeDfsSurvives100000Vertices() {
        Graph path = new Graph(100000, true);
        for (int v = 1; v < path.size(); v++) path.addEdge(v - 1, v, 1);
        Metrics metrics = new Metrics();
        assertEquals(100000, Kosaraju.run(path, metrics).count());
        assertEquals(100000, metrics.maxDepth);
        assertEquals(199998, metrics.edgeScans);
    }
}
