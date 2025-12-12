#!/usr/bin/env python3
"""
Simple plotter for JMH CSV output files. Produces PNGs (one per benchmark)
and a combined HTML page (results.html).

Usage:
  python3 plot_results.py jmh-results-<host>/*.csv

Requires:
  pip3 install --user pandas matplotlib
"""
import sys
import os
import pandas as pd
import matplotlib.pyplot as plt

if len(sys.argv) < 2:
    print("Usage: plot_results.py <csv-file> [csv-file ...]")
    sys.exit(1)

csv_files = sys.argv[1:]
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

out_dir = "plots"
os.makedirs(out_dir, exist_ok=True)
png_files = []

# If JMH CSV includes 'Threads' column, use it. Otherwise try to parse thread count from source filename.
if 'Threads' not in df.columns:
    # try to add Threads by parsing 'source_file' if it contains 't<number>'
    import re
    def extract_threads(row):
        m = re.search(r"t(\d+)", row['source_file'])
        return int(m.group(1)) if m else 1
    df['Threads'] = df.apply(extract_threads, axis=1)

for bench in sorted(df['Benchmark'].unique()):
    sub = df[df['Benchmark'] == bench]
    # group by threads and take mean score across multiple runs if present
    grouped = sub.groupby('Threads')['Score'].mean().sort_index()
    plt.figure(figsize=(7,4))
    plt.plot(grouped.index.astype(int), grouped.values, marker='o')
    plt.title(bench)
    plt.xlabel('Threads')
    plt.ylabel('Throughput (ops/sec)')
    plt.grid(True)
    fname = os.path.join(out_dir, (bench.replace('/', '_').replace(' ', '_') + '.png'))
    plt.savefig(fname, bbox_inches='tight')
    plt.close()
    png_files.append(fname)
    print("Saved", fname)

# Create a simple HTML page to show plots
html_path = os.path.join(out_dir, "results.html")
with open(html_path, "w") as h:
    h.write("<html><head><meta charset='utf-8'><title>JMH Results</title></head><body>\n")
    h.write("<h1>JMH Results</h1>\n")
    for p in png_files:
        h.write(f"<div style='margin-bottom:30px'><h3>{os.path.basename(p)}</h3>\n")
        h.write(f"<img src='{os.path.basename(p)}' style='max-width:900px; width:90%'></div>\n")
    h.write("</body></html>\n")

print("Generated HTML page:", html_path)
print("Open", html_path, "or copy plots/ to your local machine.")