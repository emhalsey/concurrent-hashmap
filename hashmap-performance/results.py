#!/usr/bin/env python3
"""
Simple CSV plotter for JMH csv output files. Requires matplotlib and pandas.
Usage:
  python3 plot_results.py results-concurrent-4t.csv results-locked-4t.csv
"""
import sys
import pandas as pd
import matplotlib.pyplot as plt

def plot(csv_files):
    for f in csv_files:
        df = pd.read_csv(f, comment='#')
        # JMH CSV layout places benchmark and score columns; adjust if needed
        if 'Benchmark' in df.columns and 'Score' in df.columns:
            df_group = df.groupby('Benchmark')['Score'].mean()
            df_group.plot(kind='bar', title=f)
            plt.tight_layout()
            plt.show()
        else:
            print("CSV doesn't have expected columns:", f)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: plot_results.py <jmh-csv-file> [...]")
    else:
        plot(sys.argv[1:])