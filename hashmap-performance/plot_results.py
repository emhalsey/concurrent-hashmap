#!/usr/bin/env python3
"""
Simple plotter for JMH CSV output files. Produces PNGs (one per benchmark)
and a combined HTML page (results.html).

Usage:
  python plot_results.py <csv-file> [csv-file ...] [--out <folder>]

Requires:
  pip install pandas matplotlib
"""
import sys
import os
import pandas as pd
import matplotlib.pyplot as plt

if len(sys.argv) < 2:
    print("Usage: plot_results.py <csv-file> [csv-file ...] [--out <folder>]")
    sys.exit(1)

# Parse arguments
csv_files = []
out_dir = "plots"  # default
args = sys.argv[1:]
if "--out" in args:
    idx = args.index("--out")
    if idx + 1 < len(args):
        out_dir = args[idx + 1]
        # remove from args so only CSVs remain
        args = args[:idx] + args[idx+2:]
csv_files = args

dfs = []
for f in csv_files:
    try:
        df = pd.read_csv(f, comment='#')
    except Exception as e:
        print(f"Skipping {f}: {e}")
        continue
    df['source_file'] = os.path.basename(f)
    dfs.append(df)

if not dfs:
    print("No valid CSVs provided.")
    sys.exit(1)

df = pd.concat(dfs, ignore_index=True)

if 'Benchmark' not in df.columns or 'Score' not in df.columns:
    print("CSV files don't have expected columns 'Benchmark' and 'Score'.")
    sys.exit(1)

os.makedirs(out_dir, exist_ok=True)
png_files = []

# If JMH CSV includes 'Threads' column, use it. Otherwise try to parse thread count from source filename.
if 'Threads' not in df.columns:
    import re
    def extract_threads(row):
        m = re.search(r"t(\d+)", row['source_file'])
        return int(m.group(1)) if m else 1
    df['Threads'] = df.apply(extract_threads, axis=1)

for bench in sorted(df['Benchmark'].unique()):
    sub = df[df['Benchmark'] == bench]
    grouped = sub.groupby('Threads')['Score'].mean().sort_index()
    plt.figure(figsize=(7,4))
    plt.plot(grouped.index.astype(int), grouped.values, marker='o')
    plt.title(bench)
    plt.xlabel('Threads')
    plt.ylabel('Throughput (ops/sec)')
    plt.grid(True)
    fname = os.path.join(out_dir, bench.replace('/', '_').replace(' ', '_') + '.png')
    plt.savefig(fname, bbox_inches='tight')
    plt.close()
    png_files.append(fname)
    print("Saved", fname)
