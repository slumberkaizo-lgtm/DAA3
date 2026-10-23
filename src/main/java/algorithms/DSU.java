package algorithms;

import metrics.Metrics;

public class DSU {
    private int[] parent;
    private int[] size;
    private int components;
    private final Metrics metrics;

    public DSU(int n, Metrics metrics) {
        this.metrics = metrics;
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        metrics.findCalls++;
        int root = x;
        while (parent[root] != root) root = parent[root];
        // A second walk points every visited vertex directly at the root.
        while (parent[x] != x) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int a = find(x);
        int b = find(y);
        if (a == b) return false;
        if (size[a] < size[b]) {
            int temp = a; a = b; b = temp;
        }
        parent[b] = a;
        size[a] += size[b];
        components--;
        metrics.unions++;
        return true;
    }

    public int components() { return components; }
}
