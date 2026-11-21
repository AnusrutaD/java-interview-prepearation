package concurrency;

import java.util.concurrent.*;

public class MT01_OddEvenPrinter {
    private static int num = 1;                     // guarded by lock -> plain int is enough
    private static final int MAX = 10;
    private static final Object lock = new Object();

    static Runnable printer(int parity) {           // parity 1 = odd, 0 = even
        return () -> {
            while (true) {
                synchronized (lock) {
                    while (num <= MAX && num % 2 != parity) {
                        try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                    }
                    if (num > MAX) {                // done: wake the other thread so it can exit too
                        lock.notifyAll();
                        return;
                    }
                    System.out.println(num + " printed by " + Thread.currentThread().getName());
                    num++;
                    lock.notifyAll();
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.execute(printer(1));
        pool.execute(printer(0));
        pool.shutdown();
        System.out.println("finished cleanly? " + pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
