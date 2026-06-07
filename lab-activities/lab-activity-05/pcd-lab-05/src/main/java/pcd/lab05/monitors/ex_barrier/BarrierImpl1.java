package pcd.lab05.monitors.ex_barrier;

/*
 * Barrier - to be implemented
 * In questa prima versione usiamo la versione raw, ovvero l'implementazione di monitor senza l'uso di libreria.
 * In generale quando si sceglie uno dei due approcci si procede sempre con quello senza mischiarlo con l'altro,
 * ovvero libreria si o libreria no.
 */
public class BarrierImpl1 implements Barrier {

    private int nParticipants, nArrived;

	public BarrierImpl1(int nParticipants) {
        this.nParticipants = nParticipants;
        this.nArrived = 0;
    }

    // Tutti i metodi pubblici synchronized
	@Override
	public synchronized void hitAndWaitAll() throws InterruptedException {
        nArrived++;
        while (nArrived < nParticipants) {
            wait();
        }
        notifyAll(); // è l'ultimo che arriva che non entra nel while e notifica tutti quelli fermi sulla wait che
        // possono procedere, tutti quelli svegliati ovvero n - 1 fanno notifyAll() il che non ha effetto visto che non
        // c'è più nessuno fermo sulla wait, si può anche fare un if-else in modo che solo l'ultimo che arriva faccia
        // notifyAll e quelli che si svegliano no, ma il comportamento non cambia.
	}

	
}
