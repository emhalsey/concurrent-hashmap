package concurrent_hashmap;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing a LockedHashMap (read/write lock over HashMap)
 * with java.util.concurrent.ConcurrentHashMap under mixed workloads.
 *
 * Two @Param values are provided so you can run different sized maps and
 * read probabilities via command line or by changing the annotation values below.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MapBenchmarks {

    @Param({"1000", "10000"})
    public int mapSize;

    // Probability of doing a read (0.0 - 1.0)
    @Param({"0.90", "0.99"})
    public double readProbability;

    private ConcurrentHashMap<Integer, Integer> concurrentMap;
    private LockedHashMap lockedMap;

    @Setup(Level.Trial)
    public void setup() {
        concurrentMap = new ConcurrentHashMap<>(mapSize * 2);
        for (int i = 0; i < mapSize; i++) {
            concurrentMap.put(i, i);
        }
        lockedMap = new LockedHashMap();
        lockedMap.init(mapSize);
    }

    // Benchmark method using ConcurrentHashMap
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public void concurrentMapOp(Blackhole bh) {
        doOperationConcurrent(bh);
    }

    // Benchmark method using LockedHashMap
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public void lockedMapOp(Blackhole bh) {
        doOperationLocked(bh);
    }

    private void doOperationConcurrent(Blackhole bh) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int key = rnd.nextInt(mapSize);
        if (rnd.nextDouble() < readProbability) {
            bh.consume(concurrentMap.get(key));
        } else {
            concurrentMap.put(key, rnd.nextInt());
        }
    }

    private void doOperationLocked(Blackhole bh) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int key = rnd.nextInt(mapSize);
        if (rnd.nextDouble() < readProbability) {
            bh.consume(lockedMap.get(key));
        } else {
            lockedMap.put(key, rnd.nextInt());
        }
    }
}