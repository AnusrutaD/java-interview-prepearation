package concurrency;

import java.util.concurrent.*;

public class MT02_ThreadLocal {
    static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        // 1. Each thread sees its OWN value
        Thread t1 = new Thread(() -> { CURRENT_USER.set("Anusruta"); sleep(); System.out.println("t1 sees: " + CURRENT_USER.get()); });
        Thread t2 = new Thread(() -> { CURRENT_USER.set("Ravi");     sleep(); System.out.println("t2 sees: " + CURRENT_USER.get()); });
        t1.start(); t2.start(); t1.join(); t2.join();

        // 2. Danger: thread pool reuses the thread, value LEAKS to the next task
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> CURRENT_USER.set("Request-A user")).get();          // forgot to remove()
        pool.submit(() -> System.out.println("Request B sees (no remove): " + CURRENT_USER.get())).get();

        // 3. Fix: always remove() in finally
        pool.submit(() -> { try { CURRENT_USER.set("Request-C user"); } finally { CURRENT_USER.remove(); } }).get();
        pool.submit(() -> System.out.println("Request D sees (with remove): " + CURRENT_USER.get())).get();
        pool.shutdown();
    }
    static void sleep() { try { Thread.sleep(50); } catch (InterruptedException e) {} }
}
