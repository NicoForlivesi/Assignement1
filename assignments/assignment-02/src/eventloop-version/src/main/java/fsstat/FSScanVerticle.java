package fsstat;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;

import java.io.File;
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
    private final Set<String> excludedDirs;

    public FSScanVerticle(String rootDir, long maxFS, int nb, Promise<FSReport> resultPromise, Set<String> excludedDirs) {
        this.rootDir = rootDir;
        this.maxFS = maxFS;
        this.nb = nb;
        this.resultPromise = resultPromise;
        this.excludedDirs = excludedDirs;
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
     * Le chiamate sulle subdirectory partono tutte in parallelo,
     * poi Future.all() controlla che siano tutte complete prima di fare il merge.
     * Le chiamate a fs.readDir() e fs.props() sono operazioni asincrone di I/O che Vert.x delega ai background thread.
     *
     * scanDirectory viene chiamata una volta per ogni directory incontrata nell'albero e ogni chiamata crea la propria promise locale.
     * Ogni promise locale si completa quando il sottoalbero radicato in quella directory è stato completamente esplorato.
     */
    private Future<FSReport> scanDirectory(FileSystem fs, String dir) {
        Promise<FSReport> promise = Promise.promise();
        FSReport report = new FSReport(nb, maxFS);

        fs.readDir(dir).onComplete(readDirRes -> { // chiamata asincrona
            if (readDirRes.failed()) { // Questo if serve per gestire il caso in cui sia stata specificata una directory
                // da analizzare in cui sono presenti sotto directory protette dal sistema operativo alla quale viene vietato l'accesso,
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
                String name = new File(entry).getName();
                if (excludedDirs.contains(name)) { // Salta le directory il cui nome è nella lista di esclusioni
                    log("Skipping excluded: " + entry);
                    continue;
                }
                Promise<Void> propsPromise = Promise.promise();
                propsFutures.add(propsPromise.future()); // Aggiungo già all'ArrayList la future associata alla propsPromise.
                // Viene poi gestito nel codice sotto il suo completamento.

                fs.props(entry).onComplete(propsRes -> {
                    if (propsRes.failed()) {
                        // Entry non accessibile
                        log("WARNING: skipping inaccessible entry: " + entry);
                        propsPromise.complete(); // Faccio una specie di bypass nel caso l'entry non sia accessibile
                        return;
                    }
                    if (propsRes.result().isRegularFile()) {
                        // File: aggiorniamo subito il report locale (siamo nell'event-loop)
                        report.addFile(propsRes.result().size());
                        propsPromise.complete();
                    } else {
                        // Directory: avviamo la scansione ricorsiva in parallelo
                        Future<FSReport> subFuture = scanDirectory(fs, entry); // Restituita la future relativa alla
                        // nuova promise locale per il nuovo sotto-albero.
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
            // Quando tutte le computazioni su props e subDir in un albero sono completate agganciamo la callback
            Future.all(propsFutures).onComplete(allRes -> {
                if (allRes.failed()) {
                    promise.fail(allRes.cause());
                    return;
                }
                for (Future<FSReport> subFuture : subDirFutures) {
                    report.merge(subFuture.result());
                }
                promise.complete(report);
            });
        });

        return promise.future(); // ScanDirectory ritorna subito la future associata alla promise locale creata
    }

    private void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "][" + Thread.currentThread().getName() + "] " + msg);
    }
}
