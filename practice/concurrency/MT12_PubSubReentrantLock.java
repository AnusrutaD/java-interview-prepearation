package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * MT12 (variant) — Publisher/Subscriber with ReentrantLock + two Conditions.
 * Key point: publisher and subscriber must share the SAME lock and conditions
 * (v1 bug: separate monitors -> subscriber was never woken).
 */
public class MT12_PubSubReentrantLock {

    static class Publisher {
        private final Queue<Integer> queue; private final ReentrantLock lock;
        private final Condition notFull, notEmpty; private final int capacity;

        Publisher(Queue<Integer> queue, ReentrantLock lock, Condition notFull, Condition notEmpty, int capacity) {
            this.queue = queue; this.lock = lock; this.notFull = notFull; this.notEmpty = notEmpty; this.capacity = capacity;
        }

        void publish(int item) {
            lock.lock();
            try {
                while (queue.size() == capacity) notFull.await();
                queue.offer(item);
                notEmpty.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException("interrupted");
            } finally {
                lock.unlock();
            }
        }
    }

    static class Subscriber {
        private final Queue<Integer> queue; private final ReentrantLock lock;
        private final Condition notFull, notEmpty;

        Subscriber(Queue<Integer> queue, ReentrantLock lock, Condition notFull, Condition notEmpty) {
            this.queue = queue; this.lock = lock; this.notFull = notFull; this.notEmpty = notEmpty;
        }

        int receive() {
            lock.lock();
            try {
                while (queue.isEmpty()) notEmpty.await();
                int item = queue.poll();
                notFull.signal();
                return item;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException("interrupted");
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Queue<Integer> q = new LinkedList<>();
        ReentrantLock lock = new ReentrantLock();
        Condition notFull = lock.newCondition(), notEmpty = lock.newCondition();
        Publisher pub = new Publisher(q, lock, notFull, notEmpty, 2);
        Subscriber sub = new Subscriber(q, lock, notFull, notEmpty);

        // 1. subscriber starts first (the case that broke in v1)
        ExecutorService p1 = Executors.newFixedThreadPool(2);
        Future<Integer> got = p1.submit(sub::receive);
        Thread.sleep(200);
        p1.execute(() -> pub.publish(42));
        System.out.println("subscriber-first: received " + got.get(2, TimeUnit.SECONDS));
        p1.shutdown();

        // 2. stress: 3 publishers x 3000, 3 subscribers, capacity 2
        Set<Integer> seen = ConcurrentHashMap.newKeySet();
        ExecutorService p2 = Executors.newFixedThreadPool(6);
        for (int t = 0; t < 3; t++) { int base = t * 3000; p2.execute(() -> { for (int i = 0; i < 3000; i++) pub.publish(base + i); }); }
        for (int t = 0; t < 3; t++) p2.execute(() -> { for (int i = 0; i < 3000; i++) seen.add(sub.receive()); });
        p2.shutdown();                                     // don't forget: without shutdown() the JVM never exits
        System.out.println("stress finished: " + p2.awaitTermination(20, TimeUnit.SECONDS)
                + ", unique received: " + seen.size() + " / 9000, left: " + q.size());
    }
}
