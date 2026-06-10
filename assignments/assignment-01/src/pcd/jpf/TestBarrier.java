package pcd.jpf;

import pcd.threads.util.CyclicBarrierImpl;

public class TestBarrier {

    private static final int N_THREADS = 3;
    private static final int N_ROUNDS = 2;

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrierImpl barrier = new CyclicBarrierImpl(N_THREADS);

        Thread[] threads = new Thread[N_THREADS];
        for (int i = 0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int round = 0; round < N_ROUNDS; round++) {
                        // Lavoro pre barriera....
                        barrier.hitAndWaitAll();
                        // Lavoro post barriera...
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}