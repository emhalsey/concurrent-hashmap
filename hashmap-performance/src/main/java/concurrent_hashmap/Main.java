package concurrent_hashmap;
/** Code I used for reference for concurrent HM:
 * https://codingtechroom.com/question/-parallel-processing-hashmap-java
 */

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

public class Main {

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
            arr[i] = new Thread();
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
            arr[i] = new Thread();
            arr[i].start();
        }
        for (Thread t : arr) t.join();

        long end = System.nanoTime();
        printResults("ConcurrentHashMap", threads, ops, start, end);
    }

    private static void printResults(String name, int threads, int ops, long start, long end) {
        long totalOps = (long) threads * ops;
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.print("%s throughput: %.2f ops/sec%n%n"+ name+ totalOps / seconds);
    }
}
