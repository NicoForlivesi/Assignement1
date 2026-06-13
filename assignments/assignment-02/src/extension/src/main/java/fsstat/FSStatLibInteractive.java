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
 * - Aggiornamenti dinamici tramite timer periodico
 *
 * Rispetto a FSStatLib aggiunge:
 * Un AtomicBoolean stopped controllato in varie porzioni del codice per mantenere reattività del tasto stop, questo
 * AtomicBoolean se a true, la ricorsione si interrompe restituendo un report vuoto, che viene poi mergiato con quello
 * trovato fin ora.
 * AtomicBoolean è necessario perché stop() viene chiamato dall' EDT mentre la scansione gira sull'event-loop, sono
 * due thread diversi.
 *
 * Un FSReport globale "live" aggiornato ad ogni sotto-albero completato.
 * Un timer periodico ne scatta uno snapshot ogni 200ms tramite getLiveReport() e lo passa alla
 * callback "onUpdate" così la GUI riceve aggiornamenti fluidi senza essere inondata da invokeLater per ogni file.
 */
public class FSStatLibInteractive {

    private final Vertx vertx = Vertx.vertx();

    // Flag di stop scritto dall'EDT e letto dall'event-loop.
    private final AtomicBoolean stopped = new AtomicBoolean(false);

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

        // Il timer scatta ogni 200ms sull'event-loop e passa uno snapshot del liveReport alla GUI.
        long timerId = vertx.setPeriodic(200, id -> // Senza questo timer l'EDT non riesce a gestire
                // l'aggiornamento sulla GUI in maniera sufficientemente fluida
                onUpdate.accept(getLiveReport())
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

    private Future<FSReportExt> scanDirectory(FileSystem fs, String dir) {
        // Se è stato richiesto lo stop, interrompo la ricorsione restituendo
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
                    if (stopped.get()) return Future.succeededFuture(new FSReportExt(nb, maxFS)); // Check anche qui per
                    // mantenere reattivo il tasto stop
                    List<Future<FSReportExt>> futures = new ArrayList<>();

                    for (String entry : entries) {
                        if (!excludedDirs.contains(new File(entry).getName())) {
                            futures.add(processEntry(fs, entry));
                        }
                    }

                    return Future.all(futures).map(ignored -> {
                        if (stopped.get()) return new FSReportExt(nb, maxFS); // Se stopped, non fare merge
                        FSReportExt report = new FSReportExt(nb, maxFS);
                        futures.forEach(f -> {
                            FSReportExt sub = f.result();
                            report.merge(sub);
                            liveReport.merge(sub); // aggiorna il live progressivamente
                        });
                        return report;
                    });
                });
    }

    private Future<FSReportExt> processEntry(FileSystem fs, String entry) {
        // Ricontrollo il flag stopped anche per ogni Entry
        if (stopped.get()) {
            return Future.succeededFuture(new FSReportExt(nb, maxFS));
        }

        // Uguale alla versione base
        return fs.props(entry)
                .recover(err -> {
                    log("WARNING: skipping inaccessible entry: " + entry);
                    return Future.succeededFuture(null);
                })
                .compose(props -> {
                    if (stopped.get()) return Future.succeededFuture(new FSReportExt(nb, maxFS)); // Check anche qui per
                    // mantenere reattivo il tasto stop.
                    if (props == null) {
                        return Future.succeededFuture(new FSReportExt(nb, maxFS));
                    }
                    if (props.isRegularFile()) {
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

    public FSReportExt getLiveReport() {
        return liveReport.snapshot();
    }
}