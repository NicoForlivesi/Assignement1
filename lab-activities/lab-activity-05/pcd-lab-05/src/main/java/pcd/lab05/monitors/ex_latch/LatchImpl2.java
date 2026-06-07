package pcd.lab05.monitors.ex_latch;

import java.util.concurrent.locks.*;

/*
 * Latch - to be implemented
 * Con libreria
 */
public class LatchImpl2 implements Latch {

    private int nCountDowns, nCounts;
    private Lock lock;
    private Condition allCountsDone;

    public LatchImpl2(int nCountDowns) {
        this.nCountDowns = nCountDowns;
        nCounts = 0;
        lock = new ReentrantLock();
        allCountsDone = lock.newCondition();
    }
	
	@Override
	public void await() throws InterruptedException {
        try {
            lock.lock();
            while (nCounts < nCountsDowns) {
                allCountsDone.await();
            }
        } finally {
            lock.unlock();
        }
    }

	@Override
	public void countDown() {
        try {
            lock.lock();
            nCounts++;
            if (nCounts == nCountsDowns) {
                allCountsDone.signalAll();
            }
        } finally {
            lock.unlock()
        }
    }
}
