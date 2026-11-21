import java.util.concurrent.locks.*;

public class MT06_MT07_Locks {
    static long start = System.currentTimeMillis();
    static void log(String s) { System.out.println((System.currentTimeMillis() - start) / 100 * 100 + "ms  " + s); }
    static void work() { try { Thread.sleep(500); } catch (InterruptedException e) {} }

    synchronized void instanceMethod()       { log("instance lock (this) START"); work(); log("instance lock END"); }
    static synchronized void staticMethod()  { log("class lock (MT06_MT07_Locks.class) START"); work(); log("class lock END"); }

    public static void main(String[] args) throws Exception {
        MT06_MT07_Locks obj = new MT06_MT07_Locks();
        Thread a = new Thread(obj::instanceMethod);
        Thread b = new Thread(MT06_MT07_Locks::staticMethod);
        a.start(); b.start(); a.join(); b.join();

        // Reentrant: the same thread re-acquires a lock it already holds
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.lock();                                  // would deadlock if not reentrant
        System.out.println("hold count = " + lock.getHoldCount());
        lock.unlock(); lock.unlock();

        // tryLock: give up instead of waiting forever
        lock.lock();
        Thread c = new Thread(() -> {
            try { System.out.println("other thread tryLock(200ms) -> " + lock.tryLock(200, java.util.concurrent.TimeUnit.MILLISECONDS)); }
            catch (InterruptedException e) {}
        });
        c.start(); c.join(); lock.unlock();
    }
}
