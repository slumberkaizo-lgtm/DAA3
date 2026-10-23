package metrics;

public class Metrics {
    public long findCalls;
    public long unions;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public long dsuOps() {
        return findCalls + unions;
    }

    public void depth(int depth) {
        maxDepth = Math.max(maxDepth, depth);
    }
}
