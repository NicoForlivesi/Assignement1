package fsstat;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Versione interattiva di FSScanVerticle con due estensioni:
 *
 * 1) STOP: prima di processare ogni entry controlla l'AtomicBoolean "stopped".
 *    Se è true, completa la promise corrente con il report parziale accumulato
 *    fino a quel momento e non lancia altre chiamate ricorsive.
 *    L'AtomicBoolean è letto dall'event-loop ma scritto dalla GUI (thread Swing):
 *    per questo serve AtomicBoolean e non un semplice boolean.
 * 2) AGGIORNAMENTI DINAMICI: ogni UPDATE_EVERY file trovati, il verticle
 *    pubblica uno "snapshot" del report corrente sull'Event Bus.
 *    La GUI è in ascolto su quel topic e aggiorna la TextArea.
 */
public class FSScanInteractiveVerticle extends VerticleBase {

    public static final String UPDATE_TOPIC = "fs.update";
    private static final int UPDATE_EVERY = 10;

    private final String rootDir;
    private final long maxFS;
    private final int nb;
    private final Promise<FSReportPartial> resultPromise;
    private final Set<String> excludedDirs;
    private final AtomicBoolean stopped;

    private final FSReportPartial globalReport;
    private long filesSinceLastUpdate = 0;

    public FSScanInteractiveVerticle(String rootDir, long maxFS, int nb,
                                     Promise<FSReportPartial> resultPromise,
                                     Set<String> excludedDirs,
                                     AtomicBoolean stopped) {
        this.rootDir = rootDir;
        this.maxFS = maxFS;
        this.nb = nb;
        this.resultPromise = resultPromise;
        this.excludedDirs = excludedDirs;
        this.stopped = stopped;
        this.globalReport  = new FSReportPartial(nb, maxFS);
    }

    @Override
    public Future<?> start() throws Exception {
        log("Starting interactive scan of: " + rootDir);
        FileSystem fs = this.vertx.fileSystem();

        scanDirectory(fs, rootDir)
                .onSuccess(report -> {
                    FSReportPartial finalReport = globalReport;
                    if (stopped.get()) { log("Scan stopped. Partial files: " + finalReport.getTotalFiles()); }
                    else { log("Scan complete. Total files: " + finalReport.getTotalFiles()); }
                    resultPromise.complete(finalReport);
                    this.vertx.undeploy(this.deploymentID());
                })
                .onFailure(err -> {
                    log("Scan failed: " + err.getMessage());
                    resultPromise.fail(err);
                    this.vertx.undeploy(this.deploymentID());
                });

        return super.start();
    }

    private Future<FSReportPartial> scanDirectory(FileSystem fs, String dir) {
        Promise<FSReportPartial> promise = Promise.promise();
        // Controlla lo stop flag prima di fare qualsiasi lavoro su questa directory
        if (stopped.get()) {
            promise.complete(globalReport);
            return promise.future();
        }

        fs.readDir(dir).onComplete(readDirRes -> {
            if (readDirRes.failed()) {
                log("WARNING: skipping inaccessible directory: " + dir);
                promise.complete(globalReport);
                return;
            }

            List<String> entries = readDirRes.result();
            if (entries.isEmpty()) {
                promise.complete(globalReport);
                return;
            }

            List<Future<Void>> propsFutures = new ArrayList<>();

            for (String entry : entries) {
                String name = new File(entry).getName();
                if (excludedDirs.contains(name)) {
                    log("Skipping excluded: " + entry);
                    continue;
                }
                Promise<Void> propsPromise = Promise.promise();
                propsFutures.add(propsPromise.future());

                if(stopped.get()) {
                    propsPromise.complete();
                    continue;
                }

                fs.props(entry).onComplete(propsRes -> {
                    // Se stopped, completiamo subito senza fare altro lavoro
                    if (stopped.get()) {
                        propsPromise.complete();
                        return;
                    }
                    if (propsRes.failed()) {
                        log("WARNING: skipping inaccessible entry: " + entry);
                        propsPromise.complete();
                        return;
                    }
                    if (propsRes.result().isRegularFile()) {
                        globalReport.addFile(propsRes.result().size());
                        checkUpdateBuffer();
                        propsPromise.complete();
                    } else {
                        // Qui non è necessario il controllo dello stop flag anche prima di ogni chiamata ricorsiva:
                        // se il check all'inizio di questo metodo fosse stato fatto dopo la chiamata asincrona a readDir
                        // allora avrebbe avuto senso per rendere il pulsante di stop più reattivo, ma avendolo fatto prima
                        // di readDir semplicemente il controllo del flag stop viene fatto subito all'inizio di ogni nuova
                        // chiamata ricorsiva.
                        scanDirectory(fs, entry).onComplete(subRes -> {
                            if (subRes.failed()) {
                                propsPromise.fail(subRes.cause());
                            } else {
                                propsPromise.complete();
                            }
                        });
                    }
                });
            }

            Future.all(propsFutures).onComplete(allRes -> {
                if (allRes.failed()) {
                    promise.fail(allRes.cause());
                } else {
                    promise.complete(globalReport);
                }
            });
        });

        return promise.future();
    }

    private void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "][" + Thread.currentThread().getName() + "] " + msg);
    }

    /**
     * Pubblica un aggiornamento sull'Event Bus ogni UPDATE_EVERY file.
     * Chiamato solo dall'event-loop quindi filesSinceLastUpdate non ha race condition.
     */
    private void checkUpdateBuffer() {
        filesSinceLastUpdate++;
        if (filesSinceLastUpdate >= UPDATE_EVERY) {
            vertx.eventBus().publish(UPDATE_TOPIC, globalReport.toJson());
            filesSinceLastUpdate = 0;
        }
    }
}
