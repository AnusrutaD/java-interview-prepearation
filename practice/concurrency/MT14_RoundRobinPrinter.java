import java.util.concurrent.*;
public class MT14_RoundRobinPrinter {
    public static void main(String[] args) throws Exception {
        Object lock = new Object();
        int[] num = {1};                                   // start at 1
        int max = 10, n = 3;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        for (int i = 0; i < n; i++) pool.execute(print(lock, num, max, n, i));
        pool.shutdown();
        System.out.println("finished cleanly? " + pool.awaitTermination(5, TimeUnit.SECONDS));
    }
    static Runnable print(Object lock, int[] num, int max, int n, int id) {
        return () -> {
            while (true) {
                synchronized (lock) {
                    while (num[0] <= max && (num[0] - 1) % n != id) {
                        try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                    }
                    if (num[0] > max) { lock.notifyAll(); return; }   // EXIT
                    System.out.println("THREAD-" + (id + 1) + " : " + num[0]);
                    num[0]++;
                    lock.notifyAll();
                }
            }
        };
    }
}
