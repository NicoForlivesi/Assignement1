package fsstat;

import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Estensione interattiva di FSStatLib con supporto a:
 * - stop() per interrompere la scansione in corso
 * - callback onUpdate chiamata dinamicamente durante la scansione
 */
public class FSStatLibInteractive {

    private final Vertx vertx = Vertx.vertx();
    private final AtomicBoolean stopped = new AtomicBoolean(false); // Questa è l'unica variabile che viene
    // condivisa fra il thread che chiama stop e l'event-loop, per questo motivo serve definirla atomica (cosa non necessaria
    // per i campi del verticle sui quali abbiamo garanzia che può accedere solamente l'event-loop)
    private MessageConsumer<?> updateConsumer;

    /**
     * Avvia la scansione e chiama onUpdate ogni UPDATE_EVERY file trovati.
     *
     * @param dir   Directory radice
     * @param maxFS        Dimensione massima file (byte)
     * @param nb           Numero di fasce
     * @param excludedDirs Directory da escludere per nome
     * @param onUpdate     Callback chiamata con il report parziale aggiornato
     * @return             Future<FSReportPartial> completata al termine (o allo stop)
     */
    public Future<FSReportPartial> getFSReport(String dir, long maxFS, int nb,
                                               Set<String> excludedDirs,
                                               Consumer<FSReportPartial> onUpdate) {
        stopped.set(false); // A ogni inizio di una nuova scansione viene risettato stopped a false nel caso la
        // precedente fosse stata interrotta
        Promise<FSReportPartial> promise = Promise.promise();

        // Registra il consumer sull'Event Bus PRIMA di deployare il verticle
        // così non perdiamo nessun aggiornamento pubblicato dal verticle
        updateConsumer = vertx.eventBus().consumer(
                FSScanInteractiveVerticle.UPDATE_TOPIC,
                msg -> onUpdate.accept(FSReportPartial.fromJson((JsonObject) msg.body()))
        );

        vertx.deployVerticle(new FSScanInteractiveVerticle(dir, maxFS, nb, promise, excludedDirs, stopped))
                .onFailure(err -> promise.fail("Deploy failed: " + err.getMessage()));

        // Quando la scansione finisce (o viene stoppata) de-registriamo il consumer
        promise.future().onComplete(res -> {
            if (updateConsumer != null) updateConsumer.unregister();
        });

        return promise.future();
    }

    /**
     * Interrompe la scansione in corso settando l'AtomicBoolean a true.
     * Il verticle lo controlla prima di ogni nuova directory e smette di
     * lanciare chiamate ricorsive, completando la Future con il report parziale.
     */
    public void stop() {
        stopped.set(true);
    }

    public boolean isStopped() { return stopped.get(); }

    public Future<Void> close() {
        return vertx.close();
    }
}