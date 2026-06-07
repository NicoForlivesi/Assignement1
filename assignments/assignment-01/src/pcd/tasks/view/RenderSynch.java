package pcd.tasks.view;

/**
 * Monitor passivo utilizzato per sincronizzare il thread del GameEngine (Master)
 * con l'Event Dispatch Thread di Swing.
 */
public class RenderSynch {

    private long currentFrame = 0;
    private long renderedFrame = 0;

    /**
     * Chiamato dal GameEngine all'inizio di ogni ciclo per registrare
     * e ottenere l'identificativo del prossimo frame da calcolare.
     */
    public synchronized long nextFrameToRender() {
        currentFrame++;
        return currentFrame;
    }

    /**
     * Chiamato dal GameEngine alla fine del ciclo. Il Master si mette in wait
     * finché l'interfaccia grafica non ha completato il disegno di questo specifico frame.
     */
    public synchronized void waitForFrameRendered(long frameId) throws InterruptedException {
        while (renderedFrame < frameId) {
            wait();
        }
    }

    /**
     * Chiamato in modo asincrono dal EDT subito dopo
     * aver completato il disegno a schermo dei componenti.
     */
    public synchronized void notifyFrameRendered() {
        renderedFrame++;
        notifyAll(); // Sveglia il GameEngine in attesa
    }
}