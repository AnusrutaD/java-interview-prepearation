import java.util.concurrent.*;

public class MT09_ThreadPools {
    public static void main(String[] args) throws Exception {
        Runnable slow = () -> { try { Thread.sleep(2000); } catch (InterruptedException e) {} };

        ThreadPoolExecutor fixed = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        for (int i = 0; i < 1000; i++) fixed.submit(slow);
        System.out.println("FIXED(2):  threads=" + fixed.getPoolSize() + ", tasks waiting in queue=" + fixed.getQueue().size());

        ThreadPoolExecutor cached = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) cached.submit(slow);
        System.out.println("CACHED:    threads=" + cached.getPoolSize() + ", tasks waiting in queue=" + cached.getQueue().size());

        ThreadPoolExecutor bounded = new ThreadPoolExecutor(
            2, 4, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadPoolExecutor.CallerRunsPolicy());
        int rejected = 0;
        ThreadPoolExecutor bounded2 = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10), new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 20; i++) { try { bounded2.submit(slow); } catch (RejectedExecutionException e) { rejected++; } }
        System.out.println("BOUNDED(core 2, max 4, queue 10) with 20 tasks: threads=" + bounded2.getPoolSize()
            + ", queued=" + bounded2.getQueue().size() + ", rejected=" + rejected);

        fixed.shutdownNow(); cached.shutdownNow(); bounded.shutdownNow(); bounded2.shutdownNow();
    }
}
