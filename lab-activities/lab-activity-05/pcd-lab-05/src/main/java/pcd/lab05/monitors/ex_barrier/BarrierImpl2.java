package pcd.lab05.monitors.ex_barrier;

import java.util.concurrent.locks.*;

/*
 * Barrier - to be implemented
 *
 * Con libreria
 */
public class BarrierImpl2 implements Barrier {

    private int nParticipants, nArrived;
    private Lock lock;
    private Condition allArrived;

	public BarrierImpl2(int nParticipants) {
        this.nParticipants = nParticipants;
        this.nArrived = 0;
        lock = new ReentrantLock();
        allArrived = lock.newCondition();
    }
	
	@Override
	public void hitAndWaitAll() throws InterruptedException {
        try {
            lock.lock;
            nArrived++;
            while (nArrived < nParticipants) {
                allArriived.await(); // ferma i thread qui e sblocca il lock corrispondente associato alla codition variable
            }
            allArrived.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
