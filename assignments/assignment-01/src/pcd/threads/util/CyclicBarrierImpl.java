package pcd.threads.util;

/**
 * Come l'esempio delle barriere che abbiamo visto in laboratorio, ma con una piccola modifica
 * ovvero l'introduzione del campo "generation" che permette alla barriera di resettarsi automaticamente
 * per il ciclo fisico successivo, serve un po come protezione:
 * gestisce il caso in cui vi sia uno sbilanciamento nei tempi di risveglio dei thread (es. causato dallo scheduler del sistema operativo).
 * Ogni secondo i threads devono sincronizzarsi tante volte (una per ogni frame), quel campo serve per proteggere
 * il caso in cui certi thread siano molto lenti a "svegliarsi", la condizione impedisce ai thread veloci che sono già
 * passati al frame successivo di "imbrogliare" o bloccare i thread lenti rimasti al frame precedente
 * L'ultimo thread che arriva incrementa il turno: generation++ e sveglia tutti per iniziare la generation successiva.*/
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