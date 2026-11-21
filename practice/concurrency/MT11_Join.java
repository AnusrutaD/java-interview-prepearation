package concurrency;

public class MT11_Join {
    public static void main(String[] args) throws Exception {
        Thread t2 = new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            System.out.println("t2 finished its work");
        });
        t2.start();
        System.out.println("main: calling t2.join()  -> main will now WAIT");
        t2.join();                                   // the CALLER (main) waits for t2
        System.out.println("main: resumed after t2 finished");
    }
}
