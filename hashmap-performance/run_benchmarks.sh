#!/usr/bin/env bash
# Run a matrix of JMH runs and save CSV results.
# Usage: ./run_benchmarks.sh
set -euo pipefail

# Adjust these if needed
JAVA="/home/dl/linux/jdk/bin/java"   # professor-provided JVM; change if you want another
JAR="$(ls target/*benchmarks.jar | head -n1)"
if [[ -z "$JAR" ]]; then
  echo "ERROR: benchmarks jar not found in target/. Build first: mvn -DskipTests clean package"
  exit 1
fi

HOST="$(hostname)"
OUTDIR="jmh-results-${HOST}-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUTDIR"

# JMH run parameters
THREADS=(4 8 16 32)         # thread counts to test
FORKS=1
WARMUP_ITERS=5
MEAS_ITERS=10
JVM_ARGS="-Xms2g -Xmx2g -XX:+UseG1GC"

# If your JMH file defines @Param values, JMH will iterate across them automatically.
# You can override specific @Param values with -p name=value. Example (commented):
# PARAM_OVERRIDES="-p mapSize=10000 -p readProbability=0.9"
PARAM_OVERRIDES=""

# Regex to match your benchmark class or methods. Adjust if needed.
# This will run all benchmarks that match "JMHBenchmarks"
BENCH_REGEX=".*JMHBenchmarks.*"

echo "Running benchmarks into $OUTDIR"
for t in "${THREADS[@]}"; do
  outfile="${OUTDIR}/results-${HOST}-t${t}.csv"
  echo "Running threads=${t} -> $outfile"
  "$JAVA" -jar "$JAR" "$BENCH_REGEX" -t "$t" \
      -wi "$WARMUP_ITERS" -i "$MEAS_ITERS" -f "$FORKS" \
      -jvmArgs "$JVM_ARGS" -rf csv -rff "$outfile" $PARAM_OVERRIDES
done

echo "Done. Results saved in $OUTDIR"
ls -lh "$OUTDIR"