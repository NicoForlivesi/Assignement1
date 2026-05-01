package fsstat;

import io.vertx.core.*;

import java.util.Set;

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
        return getFSReport(dir, maxFS, nb, Set.of()); // Se non vengono specificate directory da escludere viene richiamato
        // il costruttore passando un empty set per quell'argomento
    }

    /**
     * Come getFSReport, ma con la possibilità di escludere directory per nome.
     *
     * @param excludedDirs Nomi di directory da escludere (es. "AppData" per non incorrere in problemi derivanti dal O.S.)
     */
    public Future<FSReport> getFSReport(String dir, long maxFS, int nb, Set<String> excludedDirs) {
        Promise<FSReport> promise = Promise.promise(); // Questa è la promise che sarà completata dal verticle quando
        // la scansione finisce

        // Deploya il Verticle che farà la scansione, quando finisce completa la promise
        vertx.deployVerticle(new FSScanVerticle(dir, maxFS, nb, promise, excludedDirs))
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
