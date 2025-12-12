package concurrent_hashmap;

    /**
     * Implementing hashmap with locks
     * https://sam-wei.medium.com/the-right-choice-for-thread-safe-hashmaps-in-java-23b12b3f37be
     */

    /** Code I used for reference for concurrent HM:
     * https://codingtechroom.com/question/-parallel-processing-hashmap-java
     */

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

public class Main {

    // Synchronized HashMap
    static class LockedHashMap {
        private final HashMap<String, Integer> map = new HashMap<>();

        public synchronized Integer get(String key) {
            return map.get(key);
        }

        public synchronized void put(String key, Integer value) {
            map.put(key, value);
        }
    }

    // Worker thread
    static class Worker implements Runnable {
        private final Random rand = new Random();
        private final int ops;
        private final boolean useLocked;
        private final LockedHashMap lockedMap;
        private final ConcurrentHashMap<String, Integer> concurrentMap;

        public Worker(LockedHashMap lockedMap, ConcurrentHashMap<String, Integer> concurrentMap,
                      int ops, boolean useLocked) {
            this.lockedMap = lockedMap;
            this.concurrentMap = concurrentMap;
            this.ops = ops;
            this.useLocked = useLocked;
        }

        @Override
        public void run() {
            for (int i = 0; i < ops; i++) {
                int key = rand.nextInt(1000);
                String k = "k" + key;

                if (useLocked) {
                    if (rand.nextBoolean()) lockedMap.put(k, key);
                    else lockedMap.get(k);
                } else {
                    if (rand.nextBoolean()) concurrentMap.put(k, key);
                    else concurrentMap.get(k);
                }
            }
        }
    }

    // Benchmark runner
    public static void main(String[] args) throws InterruptedException {
        int threads = 8;
        int opsPerThread = 100_000;

        System.out.println("Testing LockedHashMap...");
        runTestLocked(threads, opsPerThread);

        System.out.println("Testing ConcurrentHashMap...");
        runTestConcurrent(threads, opsPerThread);
    }

    private static void runTestLocked(int threads, int ops) throws InterruptedException {
        LockedHashMap map = new LockedHashMap();
        Thread[] arr = new Thread[threads];

        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            arr[i] = new Thread(new Worker(map, null, ops, true));
            arr[i].start();
        }
        for (Thread t : arr) t.join();

        long end = System.nanoTime();
        printResults("LockedHashMap", threads, ops, start, end);
    }

    private static void runTestConcurrent(int threads, int ops) throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        Thread[] arr = new Thread[threads];

        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            arr[i] = new Thread(new Worker(null, map, ops, false));
            arr[i].start();
        }
        for (Thread t : arr) t.join();

        long end = System.nanoTime();
        printResults("ConcurrentHashMap", threads, ops, start, end);
    }

    private static void printResults(String name, int threads, int ops, long start, long end) {
        long totalOps = (long) threads * ops;
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("%s throughput: %.2f ops/sec%n%n", name, totalOps / seconds);
    }
}
