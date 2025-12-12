package concurrent_hashmap;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JMHBenchmarks {

    // -------------------------
    // Custom synchronized HashMap
    // -------------------------
    static class LockedHashMap {
        private final HashMap<String, Integer> map = new HashMap<>();

        public synchronized Integer get(String key) {
            return map.get(key);
        }

        public synchronized void put(String key, Integer value) {
            map.put(key, value);
        }
    }

    LockedHashMap lockedMap;
    ConcurrentHashMap<String, Integer> concurrentMap;

    @Setup
    public void setup() {
        lockedMap = new LockedHashMap();
        concurrentMap = new ConcurrentHashMap<>();

        // preload some values
        for (int i = 0; i < 1000; i++) {
            lockedMap.put("k" + i, i);
            concurrentMap.put("k" + i, i);
        }
    }

    // -------------------------
    // Benchmarks
    // -------------------------

    @Benchmark
    public void testLockedMapRead() {
        lockedMap.get("k500");
    }

    @Benchmark
    public void testLockedMapWrite() {
        lockedMap.put("k500", 123);
    }

    @Benchmark
    public void testConcurrentMapRead() {
        concurrentMap.get("k500");
    }

    @Benchmark
    public void testConcurrentMapWrite() {
        concurrentMap.put("k500", 123);
    }
}
