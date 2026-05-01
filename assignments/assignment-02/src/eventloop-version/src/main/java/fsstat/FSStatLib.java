package fsstat;

import io.vertx.core.*;

public class FSStatLib {

    private final Vertx vertx = Vertx.vertx();

    /**
     * Calcola in modo asincrono le statistiche sul filesystem della directory D.
     *
     * @param dir   La directory radice da scansionare (ricorsivamente)
     * @param maxFS La dimensione massima di file considerata (in byte)
     * @param nb    Il numero di fasce (bands) in cui suddividere [0, maxFS]
     * @return      Una Future<FSReport> che si completerà con il report
     */
    public Future<FSReport> getFSReport(String dir, long maxFS, int nb) {
        Promise<FSReport> promise = Promise.promise(); // Questa è la promise che sarà completata dal verticle quando
        // la scansione finisce

        // Deploya il Verticle che farà la scansione, quando finisce completa la promise
        vertx.deployVerticle(new FSScanVerticle(dir, maxFS, nb, promise))
                .onFailure(err -> promise.fail("Deploy failed: " + err.getMessage()));

        return promise.future(); // Restituiamo subito la Future (non bloccante)
    }

    /**
     * Chiude l'istanza Vertx. Chiamare quando non si usa più la libreria.
     */
    public Future<Void> close() {
        return vertx.close();
    }
}
