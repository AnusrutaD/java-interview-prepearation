package concurrency;

import java.util.*;
import java.util.concurrent.*;

/**
 * MT12 — Blocking queue with wait()/notifyAll().
 * Rules: synchronized put/take, WHILE (not if) around wait(), notifyAll() after every change.
 * main() = stress test: 2 producers x 5000 items, 2 consumers, capacity 2.
 */
public class MT12_CustomBlockingQueue {

    static class CustomBlockingQueue<T> {
        private final int capacity;
        private final Queue<T> queue = new LinkedList<>();

        CustomBlockingQueue(int capacity) { this.capacity = capacity; }

        public synchronized void put(T item) {
            while (queue.size() == capacity) {           // while: re-check after every wake-up (spurious wake-ups)
                try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            }
            queue.offer(item);
            notifyAll();                                  // wake waiting consumers
        }

        public synchronized T take() {
            while (queue.isEmpty()) {
                try { wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            }
            T item = queue.poll();
            notifyAll();                                  // wake waiting producers
            return item;
        }

        public synchronized int size() { return queue.size(); }
    }

    public static void main(String[] args) throws Exception {
        CustomBlockingQueue<Integer> q = new CustomBlockingQueue<>(2);
        int perProducer = 5000;
        Set<Integer> consumed = ConcurrentHashMap.newKeySet();
        List<Integer> dupes = Collections.synchronizedList(new ArrayList<>());

        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int p = 0; p < 2; p++) {
            int base = p * perProducer;
            pool.submit(() -> { for (int i = 0; i < perProducer; i++) q.put(base + i); });
        }
        for (int c = 0; c < 2; c++) {
            pool.submit(() -> { for (int i = 0; i < perProducer; i++) { int v = q.take(); if (!consumed.add(v)) dupes.add(v); } });
        }
        pool.shutdown();
        boolean done = pool.awaitTermination(20, TimeUnit.SECONDS);

        System.out.println("finished without hanging: " + done);
        System.out.println("items consumed: " + consumed.size() + " / 10000, duplicates: " + dupes.size() + ", left in queue: " + q.size());
    }
}
