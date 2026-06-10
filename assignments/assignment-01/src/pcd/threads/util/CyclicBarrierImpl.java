package pcd.threads.util;

/**
 * Come l'esempio delle barriere che abbiamo visto in laboratorio, ma con una piccola modifica
 * ovvero l'introduzione del campo "generation" che permette alla barriera di resettarsi automaticamente
 * per il ciclo fisico successivo senza problemi, senza dover creare una nuova istanza dell'oggetto:
 * il campo generation è un po come se fosse il turno, i thread che arrivano si mettono in attesa se currentGeneration == generation,
 * l'ultimo che arriva vede nArrived == nParticipants, entra nell'if, azzera nArrived per il setup
 * della barriera per il turno successivo, incrementa il numero del turno e sveglia tutti, i thread svegliati vedono
 * che la condizione di attesa non è più rispettata e partono e la barriera è già pronta per il turno successivo.
 * Questo concetto di fare il check sul numero del turno nel while e non per esempio su nArrived < nParticipants è
 * fondamentale per una barriera ciclica sennò avverrebbe deadlock sicuramente!! Un thread veloce bloccato sulla wait
 * del turno 2 e tutti gli altri bloccanti sulla wait del turno 1 e nessuno che incrementerebbe più nArrived.
 * */
public class CyclicBarrierImpl implements Barrier {

    private final int nParticipants;
    private int nArrived;
    private int generation;

    public CyclicBarrierImpl(int nParticipants) {
        this.nParticipants = nParticipants;
        this.nArrived = 0;
        this.generation = 0;
    }

    @Override
    public synchronized void hitAndWaitAll() throws InterruptedException {
        // Memorizzo la generazione a cui appartiene questo thread
        int currentGeneration = generation;
        nArrived++;

        if (nArrived == nParticipants) {
            // L'ultimo arrivato resetta il contatore, avanza la generazione e sveglia tutti
            nArrived = 0;
            generation++;
            notifyAll();
        } else {
            // Gli altri aspettano che la generazione cambi (segno che tutti sono arrivati)
            while (currentGeneration == generation) {
                wait();
            }
        }
    }
}