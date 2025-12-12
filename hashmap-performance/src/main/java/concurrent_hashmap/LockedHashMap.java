package concurrent_hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * HashMap wrapper protected by a ReentrantReadWriteLock.
 * get() uses read lock; put() uses write lock.
 */
public final class LockedHashMap {
    private final Map<Integer, Integer> map = new HashMap<>();
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();

    public void init(int entries) {
        Lock w = rw.writeLock();
        w.lock();
        try {
            map.clear();
            for (Integer i = (Integer) 0; i < entries; i++) {
                map.put(i, i);
            }
        } finally {
            w.unlock();
        }
    }

    public Integer get(Integer key) {
        Lock r = rw.readLock();
        r.lock();
        try {
            return map.get(key);
        } finally {
            r.unlock();
        }
    }

    public void put(Integer key, Integer value) {
        Lock w = rw.writeLock();
        w.lock();
        try {
            map.put(key, value);
        } finally {
            w.unlock();
        }
    }

    public int size() {
        Lock r = rw.readLock();
        r.lock();
        try {
            return map.size();
        } finally {
            r.unlock();
        }
    }
}