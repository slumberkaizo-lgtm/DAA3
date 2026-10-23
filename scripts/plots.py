"""Run from the project root: python scripts/plots.py."""
import csv
import math
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
with (ROOT / "results/results.csv").open() as file:
    rows = list(csv.DictReader(file))

ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = ["#1565c0", "#e65100", "#2e7d32", "#6a1b9a", "#c62828"]
OUTPUT = ROOT / "results/plots"
OUTPUT.mkdir(parents=True, exist_ok=True)


def series(algorithm, density):
    return sorted((r for r in rows if r["algorithm"] == algorithm and r["graph"] == density),
                  key=lambda r: int(r["n"]))


def operations(row):
    return int(row["dsu_ops"]) + int(row["edge_scans"]) + int(row["comparisons"])


def slope(x, y):
    x, y = [math.log(v) for v in x], [math.log(v) for v in y]
    mx, my = sum(x) / len(x), sum(y) / len(y)
    return sum((a - mx) * (b - my) for a, b in zip(x, y)) / sum((a - mx) ** 2 for a in x)


plt.rcParams.update({"font.size": 10, "figure.dpi": 160})
for filename, title, ylabel, log_y in [
    ("operations.png", "Counted operations", "find + successful union + scans + weight comparisons", True),
    ("scans_per_edge.png", "Scans per input edge", "edge_scans / m", False),
]:
    fig, ax = plt.subplots(figsize=(11, 5.4))
    for name, color in zip(ALGORITHMS, COLORS):
        for density, style in [("sparse", "-"), ("dense", "--")]:
            data = series(name, density)
            x = [int(r["n"]) for r in data]
            y = [operations(r) if log_y else int(r["edge_scans"]) / int(r["m"]) for r in data]
            ax.plot(x, y, style, color=color, marker="o", markersize=4, label=f"{name}, {density}")
    ax.set_xscale("log")
    if log_y:
        ax.set_yscale("log")
    else:
        ax.set_ylim(0, 2.15)
        ax.text(0.02, 0.06, "Prim and Kosaraju overlap at 2; Kahn and DAG overlap at 1.",
                transform=ax.transAxes, fontsize=9)
    ax.set(title=title, xlabel="Vertices n (log scale)", ylabel=ylabel)
    ax.grid(True, which="both", alpha=0.22)
    ax.legend(loc="center left", bbox_to_anchor=(1.02, 0.5), fontsize=9)
    fig.tight_layout()
    fig.savefig(OUTPUT / filename)
    plt.close(fig)

fig, ax = plt.subplots(figsize=(8, 4.8))
for density, style in [("sparse", "-"), ("dense", "--")]:
    data = series("Kruskal", density)
    ax.plot([int(r["n"]) for r in data],
            [int(r["edge_scans"]) / int(r["m"]) for r in data],
            style, marker="o", label=density)
ax.set_xscale("log")
ax.set_ylim(0, 1.05)
ax.set(title="Edges examined by Kruskal after sorting", xlabel="Vertices n (log scale)",
       ylabel="edge_scans / m")
ax.grid(True, which="both", alpha=0.22)
ax.legend()
fig.tight_layout()
fig.savefig(OUTPUT / "kruskal_share.png")
plt.close(fig)

with (ROOT / "results/analysis.csv").open("w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow(["algorithm", "density", "scan_slope", "operation_slope", "min_normalized", "max_normalized"])
    for name in ALGORITHMS:
        for density in ["sparse", "dense"]:
            data = series(name, density)
            x = [int(r["n"]) for r in data]
            scans = [int(r["edge_scans"]) for r in data]
            ops = [operations(r) for r in data]
            if name in ["Kruskal", "Prim"]:
                ratios = [int(r["comparisons"]) / (int(r["m"]) * math.log2(int(r["m"]))) for r in data]
            else:
                ratios = [int(r["edge_scans"]) / (int(r["n"]) + int(r["m"])) for r in data]
            writer.writerow([name, density, f"{slope(x, scans):.4f}", f"{slope(x, ops):.4f}",
                             f"{min(ratios):.4f}", f"{max(ratios):.4f}"])
print("Saved three plots and results/analysis.csv")
