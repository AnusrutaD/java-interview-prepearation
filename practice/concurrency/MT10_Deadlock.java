package concurrency;

import java.lang.management.*;

public class MT10_Deadlock {
    static final Object A = new Object(), B = new Object(), C = new Object(), D = new Object();
    static void pause() { try { Thread.sleep(100); } catch (InterruptedException e) {} }

    public static void main(String[] args) throws Exception {
        // T1 locks A then B; T2 locks B then A  -> circular wait
        Thread t1 = new Thread(() -> { synchronized (A) { pause(); synchronized (B) { System.out.println("t1 done"); } } }, "T1");
        Thread t2 = new Thread(() -> { synchronized (B) { pause(); synchronized (A) { System.out.println("t2 done"); } } }, "T2");
        t1.setDaemon(true); t2.setDaemon(true);
        t1.start(); t2.start();
        Thread.sleep(500);

        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        System.out.println("Deadlocked threads found: " + (ids == null ? 0 : ids.length));
        for (ThreadInfo ti : mx.getThreadInfo(ids))
            System.out.println("  " + ti.getThreadName() + " is BLOCKED waiting for lock held by " + ti.getLockOwnerName());

        // FIX: same lock ORDER in both threads (always A, then B)
        Thread f1 = new Thread(() -> { synchronized (C) { pause(); synchronized (D) { System.out.println("fixed t1 done"); } } });
        Thread f2 = new Thread(() -> { synchronized (C) { pause(); synchronized (D) { System.out.println("fixed t2 done"); } } });
        f1.start(); f2.start(); f1.join(); f2.join();
    }
}
