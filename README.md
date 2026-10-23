# DAA Assignment 3

Simple Java 17 implementation of DSU, minimum spanning forests, strongly connected
components, topological sorting and shortest paths in a DAG.

Repository: https://github.com/slumberkaizo-lgtm/DAA3

## Run

Install **JDK 17 or newer** and set `JAVA_HOME` to that JDK. A JRE alone is not enough.
Gradle 8.14.3 is included through the wrapper; the first run needs internet access.

Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat demo
.\gradlew.bat run
```

Linux/macOS:

```sh
sh gradlew test
sh gradlew demo
sh gradlew run
```

`test` runs JUnit 5, including the 100,000-vertex stack test. `demo` reads the three
files in `data/` and prints example results, a detected cycle and a restored path.
`run` runs the full benchmark: five sizes, two densities, five algorithms and five
runs per measurement. It needs up to 2 GB of Java heap. It writes 50 rows to
`results/results.csv` and checks all ten reference triples from the assignment.
A mismatch fails the command; the existing final CSV is only replaced on success.
Tests can also be opened as HTML in `build/reports/tests/test/index.html`.

To regenerate the three plots (Python 3.10+):

```sh
python -m pip install -r scripts/requirements.txt
python scripts/plots.py
```

Java and JUnit do not depend on Python. Committed PNGs can be viewed without it.

## Files

- `graph/Graph.java`: adjacency lists and text input for both graph kinds.
- `algorithms/DSU.java`: union by size and iterative path compression.
- `algorithms/MST.java`: Kruskal and lazy priority-queue Prim; both return forests.
- `algorithms/Kosaraju.java`: two iterative DFS passes, no recursive calls.
- `algorithms/Kahn.java`: topological order and cycle detection.
- `algorithms/DagShortestPath.java`: all sources at zero, negative edges, parent paths.
- `metrics/Metrics.java`: counters updated during actual algorithm operations.
- `benchmark/`: seeded generator, benchmark and small console demo.
- `REPORT.md`: complexity, measured results, correctness and algorithm selection.

Java files live in `src/main/java/`; JUnit tests are in `src/test/java/`.
No graph library or framework is used. `ArrayList`, sorting, queues and heaps are
standard Java collections; graph algorithms are implemented in the project.

## Input format

```text
6 6 directed
0 2 4
1 2 2
2 3 -5
1 4 3
4 5 1
3 5 2
```

The header is `n m directed|undirected`, followed by exactly `m` weighted edges.
Vertices are `0..n-1`. Undirected edges are stored once in the edge list and twice
in adjacency lists. Self-loops and parallel edges are allowed. Weights are `int`;
tree totals and distances use `long` to prevent accumulated integer overflow.
MST rejects directed input. Kahn and DAG paths report an exception for cycles.

## Submission

The release is `main`, tag `v1.0`. Submit a ZIP named
`DAA_Assignment3_name_surname_group.zip`, replacing the placeholders with your
details, and include the repository link. Generated `build/` and `.gradle/` folders
are not needed. The assignment's detailed benchmark specification requests ten
size/density combinations and 50 CSV rows; that is the interpretation used here.

## AI-use acknowledgement

OpenAI Codex assisted with implementation, tests, benchmark execution and
documentation. This assistance is disclosed as required by the assignment.
