package fsstat;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Estensione interattiva di FSStatLib con supporto per:
 * - Stop asincrono della scansione tramite AtomicBoolean
 * - Aggiornamenti dinamici tramite timer periodico (throttling)
 *
 * Rispetto a FSStatLib aggiunge:
 * 1. Un AtomicBoolean stopped controllato prima di ogni processEntry:
 *    se true, la ricorsione si interrompe restituendo un report vuoto.
 *    AtomicBoolean è necessario perché stop() viene chiamato dal thread Swing (EDT)
 *    mentre la scansione gira sull'event-loop — serve visibilità cross-thread.
 *
 * 2. Un FSReport globale "live" aggiornato ad ogni file trovato.
 *    Un timer periodico (vertx.setPeriodic) ne scatta uno snapshot ogni 200ms
 *    e lo passa alla callback onUpdate — così la GUI riceve aggiornamenti fluidi
 *    senza essere inondata da 27000 invokeLater (uno per file).
 */
public class FSStatLibInteractive {

    private final Vertx vertx = Vertx.vertx();

    // Flag di stop: scritto dal thread Swing (EDT), letto dall'event-loop.
    // AtomicBoolean garantisce visibilità cross-thread senza sincronizzazione esplicita.
    // È final perché non riassegniamo l'oggetto, usiamo .set() per modificarne il valore.
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    // Report "live" globale: accumulatore aggiornato ad ogni file trovato.
    // Acceduto solo dall'event-loop → nessuna race condition.
    private FSReportExt liveReport;

    // Parametri della scansione corrente, impostati a ogni chiamata di getFSReport.
    private long maxFS;
    private int nb;
    private Set<String> excludedDirs;

    public Future<FSReportExt> getFSReport(String dir, long maxFS, int nb,
                                        Set<String> excludedDirs,
                                        Consumer<FSReportExt> onUpdate) {
        stopped.set(false);
        this.maxFS = maxFS;
        this.nb = nb;
        this.excludedDirs = excludedDirs;
        this.liveReport = new FSReportExt(nb, maxFS);

        // Il timer scatta ogni 200ms sull'event-loop: fa uno snapshot del report live
        // e lo passa alla GUI. Così invece di un invokeLater per ogni file,
        // ne arrivano circa 5 al secondo — fluidi e gestibili dall'EDT di Swing.
        long timerId = vertx.setPeriodic(200, id ->
                onUpdate.accept(liveReport.snapshot())
        );

        return scanDirectory(vertx.fileSystem(), dir)
                .map(finalReport -> {
                    vertx.cancelTimer(timerId); // ferma il timer quando la scansione finisce
                    return finalReport;
                });
    }

    /** Segnala all'event-loop di interrompere la scansione al prossimo controllo. */
    public void stop() {
        stopped.set(true);
    }

    public boolean isStopped() {
        return stopped.get();
    }

    public Future<Void> close() {
        return vertx.close();
    }

    // -------------------------------------------------------------------------
    // Implementazione privata
    // -------------------------------------------------------------------------

    private Future<FSReportExt> scanDirectory(FileSystem fs, String dir) {
        // Se è stato richiesto lo stop, interrompiamo la ricorsione restituendo
        // un report vuoto: la Future si completa subito senza fare altre I/O.
        if (stopped.get()) {
            return Future.succeededFuture(new FSReportExt(nb, maxFS));
        }

        return fs.readDir(dir)
                .recover(err -> {
                    log("WARNING: skipping inaccessible directory: " + dir);
                    return Future.succeededFuture(List.of());
                })
                .compose(entries -> {
                    List<Future<FSReportExt>> futures = new ArrayList<>();

                    for (String entry : entries) {
                        if (!excludedDirs.contains(new File(entry).getName())) {
                            futures.add(processEntry(fs, entry));
                        }
                    }

                    return Future.all(futures).map(ignored -> {
                        FSReportExt report = new FSReportExt(nb, maxFS);
                        futures.forEach(f -> report.merge(f.result()));
                        return report;
                    });
                });
    }

    private Future<FSReportExt> processEntry(FileSystem fs, String entry) {
        if (stopped.get()) {
            return Future.succeededFuture(new FSReportExt(nb, maxFS));
        }

        return fs.props(entry)
                .recover(err -> {
                    log("WARNING: skipping inaccessible entry: " + entry);
                    return Future.succeededFuture(null);
                })
                .compose(props -> {
                    if (props == null) {
                        return Future.succeededFuture(new FSReportExt(nb, maxFS));
                    }
                    if (props.isRegularFile()) {
                        // Aggiorniamo solo il report live: il timer periodico
                        // si occupa di notificare la GUI a intervalli regolari.
                        liveReport.addFile(props.size());

                        FSReportExt r = new FSReportExt(nb, maxFS);
                        r.addFile(props.size());
                        return Future.succeededFuture(r);
                    } else {
                        return scanDirectory(fs, entry);
                    }
                });
    }

    private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}