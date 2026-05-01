package fsstat;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Verticle che esegue la scansione asincrona di una directory.
 *
 * - Tutto il codice qui è eseguito dall'event-loop (VerticleBase), mai da altri thread,
 * questo ci permette di definire FSReport mutabile con la garanzia che non ci saranno race condition
 * visto che è sempre l'event-loop che si occupa di effettuare aggiornamenti sulla struttura dati.
 */
public class FSScanVerticle extends VerticleBase {

    private final String rootDir;
    private final long maxFS;
    private final int nb;
    private final Promise<FSReport> resultPromise;
//    private final Set<String> excludedDirs;

    public FSScanVerticle(String rootDir, long maxFS, int nb, Promise<FSReport> resultPromise) {
        this.rootDir = rootDir;
        this.maxFS = maxFS;
        this.nb = nb;
        this.resultPromise = resultPromise;
//        this.excludedDirs = excludedDirs;
    }

    @Override // Override del metodo "start()" di VerticleBase, questo metodo viene chiamato automaticamente quando
    // facciamo il deploy di un Verticle.
    public Future<?> start() throws Exception {
        log("Starting scan of: " + rootDir);
        FileSystem fs = this.vertx.fileSystem();

        scanDirectory(fs, rootDir)
                .onSuccess(report -> {
                    log("Scan complete. Total files: " + report.getTotalFiles());
                    resultPromise.complete(report);
                    // Undeploy il verticle dopo aver completato il lavoro
                    this.vertx.undeploy(this.deploymentID());
                })
                .onFailure(err -> {
                    log("Scan failed: " + err.getMessage());
                    resultPromise.fail(err);
                    this.vertx.undeploy(this.deploymentID());
                });

        return super.start();
    }

    /**
     * Scansione ricorsiva asincrona.
     *
     * Per ogni entry della directory:
     *   - se è un file -> aggiunge la sua size al report
     *   - se è una dir -> chiama ricorsivamente scanDirectory
     * Le chiamate sulle subdirectory partono tutte in parallelo (come es. 4 del PDF),
     * poi Future.all() aspetta che siano tutte complete prima di fare il merge.
     */
    private Future<FSReport> scanDirectory(FileSystem fs, String dir) {
        Promise<FSReport> promise = Promise.promise();
        FSReport report = new FSReport(nb, maxFS);

        fs.readDir(dir).onComplete(readDirRes -> {
            if (readDirRes.failed()) { // Questo if serve per gestire il caso in cui sia stata specificata una directory
                // da analizzare in cui sono presenti file protetti dal sistema operativo alla quale viene vietato l'accesso,
                // per evitare che l'errore venga propagato semplicemente salto questa directory (specificando qual'è
                // la directory alla quale non si potuto accedere)
                log("WARNING: skipping inaccessible directory: " + dir);
                promise.complete(report);
                return;
            }

            List<String> entries = readDirRes.result();
            if (entries.isEmpty()) {
                promise.complete(report);
                return;
            }

            List<Future<Void>> propsFutures = new ArrayList<>();
            List<Future<FSReport>> subDirFutures = new ArrayList<>();

            for (String entry : entries) {
                Promise<Void> propsPromise = Promise.promise();
                propsFutures.add(propsPromise.future());

                fs.props(entry).onComplete(propsRes -> {
                    if (propsRes.failed()) {
                        // Entry non accessibile, la saltiamo
                        log("WARNING: skipping inaccessible entry: " + entry);
                        propsPromise.complete();
                        return;
                    }
                    if (propsRes.result().isRegularFile()) {
                        // File: aggiorniamo subito il report locale (siamo nell'event-loop)
                        report.addFile(propsRes.result().size());
                        propsPromise.complete();
                    } else {
                        // Directory: avviamo la scansione ricorsiva in parallelo
                        Future<FSReport> subFuture = scanDirectory(fs, entry);
                        subDirFutures.add(subFuture);
                        subFuture.onComplete(subRes -> {
                            if (subRes.failed()) {
                                propsPromise.fail(subRes.cause());
                            } else {
                                propsPromise.complete();
                            }
                        });
                    }
                });
            }

            // Aspettiamo che props e tutte le subdirectory siano completate
            Future.all(propsFutures).onComplete(allRes -> {
                if (allRes.failed()) {
                    promise.fail(allRes.cause());
                    return;
                }
                // Merge dei report delle subdirectory nel report corrente
                for (Future<FSReport> subFuture : subDirFutures) {
                    report.merge(subFuture.result());
                }
                promise.complete(report);
            });
        });

        return promise.future();
    }

    private void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "][" + Thread.currentThread().getName() + "] " + msg);
    }
}
