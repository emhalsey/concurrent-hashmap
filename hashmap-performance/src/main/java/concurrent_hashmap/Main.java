package concurrent_hashmap;
/** Code I used for reference for concurrent HM:
 * https://codingtechroom.com/question/-parallel-processing-hashmap-java
 */

/**
 * Small functional tester (outside of JMH) to check up on LockedHashMap vs ConcurrentHashMap.
 * Using this for quick correctness/throughput sanity checks.
 *
 * Usage:
 *   java -cp target/classes concurrent_hashmap.Main [threads] [opsPerThread] [mapSize] [readProbability]
 * Example:
 *   java -cp target/classes concurrent_hashmap.Main 8 100000 10000 0.9
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        int opsPerThread = args.length > 1 ? Integer.parseInt(args[1]) : 100_000;
        int mapSize = args.length > 2 ? Integer.parseInt(args[2]) : 10_000;
        double readProbability = args.length > 3 ? Double.parseDouble(args[3]) : 0.9;

        System.out.printf("Config: threads=%d ops/thread=%d mapSize=%d readProb=%.2f%n",
                threads, opsPerThread, mapSize, readProbability);

        System.out.println("Testing LockedHashMap...");
        runTestLocked(threads, opsPerThread, mapSize, readProbability);

        System.out.println("Testing ConcurrentHashMap...");
        runTestConcurrent(threads, opsPerThread, mapSize, readProbability);
    }

    private static void runTestLocked(int threads, int ops, int mapSize, double readProb) throws InterruptedException {
        LockedHashMap map = new LockedHashMap();
        map.init(mapSize);

        Thread[] arr = new Thread[threads];

        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            arr[i] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int k = 0; k < ops; k++) {
                    int key = rnd.nextInt(mapSize);
                    if (rnd.nextDouble() < readProb) {
                        map.get(key);
                    } else {
                        map.put(key, rnd.nextInt());
                    }
                }
            }, "locked-worker-" + i);
            arr[i].start();
        }
        for (Thread t : arr) t.join();

        long end = System.nanoTime();
        printResults("LockedHashMap", threads, ops, start, end);
    }

    private static void runTestConcurrent(int threads, int ops, int mapSize, double readProb) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < mapSize; i++) map.put(i, i);

        Thread[] arr = new Thread[threads];

        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            arr[i] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int k = 0; k < ops; k++) {
                    int key = rnd.nextInt(mapSize);
                    if (rnd.nextDouble() < readProb) {
                        map.get(key);
                    } else {
                        map.put(key, rnd.nextInt());
                    }
                }
            }, "concurrent-worker-" + i);
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