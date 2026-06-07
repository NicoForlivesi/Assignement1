package pcd.lab05.monitors.ex_latch;

/*
 * Latch - to be implemented
 * Versione RAW di monitor
 */
public class LatchImpl1 implements Latch {

    private int nCountDowns, nCounts;

	public LatchImpl1(int nCountDowns) {
        this.nCountDowns = nCountDowns;
        nCounts = 0;
    }
	
	@Override
	public synchronized void await() throws InterruptedException {
        while (nCounts < nCountsDowns) {
            wait();
        }
    }

	@Override
	public synchronized void countDown() {
        nCounts++;
        if (nCounts == nCountsDowns) { // Funzionerebbe anche senza questo if, sveglierebbe i thread che poi ricontrollano
            // la condizione nel while e tornerebbero a dormire, per maggiore elganza svegliamo solo quando devono
            // effettivamente partire quindi aggiungiamo l'if
            notifyAll();
        }
    }

	
}
