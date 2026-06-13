package fsstat;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Libreria per il calcolo asincrono di statistiche sul filesystem.
 *
 * Approccio event-loop puro con Vert.x:
 * - Nessun blocco, nessun thread esplicito: tutto il lavoro I/O è delegato
 *   ai worker thread interni di Vert.x (readDir, props), i risultati tornano
 *   sempre sull'event-loop tramite le callback.
 * - FSReport è mutabile ma sicuro: viene modificato solo dall'event-loop,
 *   che è single-thread, quindi nessuna race-condition.
 */
public class FSStatLib {

    private final Vertx vertx = Vertx.vertx();

    private long maxFS;
    private int nb;
    private Set<String> excludedDirs;

    /**
     * Calcola asincronamente le statistiche del filesystem di dir.
     *
     * @param dir Directory radice da scansionare (ricorsivamente)
     * @param maxFS Dimensione massima considerata
     * @param nb Numero di fasce in cui suddividere
     * @return Future che si completa con il report
     */
    public Future<FSReport> getFSReport(String dir, long maxFS, int nb) {
        return getFSReport(dir, maxFS, nb, Set.of());
    }

    /**
     * Overload di getFSReport con la possibilità di escludere directory per nome,
     * utile quando la directory radice contiene sotto-directory di sistema
     * non accessibili in lettura.
     */
    public Future<FSReport> getFSReport(String dir, long maxFS, int nb, Set<String> excludedDirs) {
        this.maxFS = maxFS;
        this.nb = nb;
        this.excludedDirs = excludedDirs;
        return scanDirectory(vertx.fileSystem(), dir);
    }

    /**
     * Chiude l'istanza Vert.x. Chiamare quando non si usa più la libreria.
     */
    public void close() {
        vertx.close();
    }

    /**
     * Scansione ricorsiva asincrona di una directory.
     *
     * Pipeline per ogni directory:
     *   readDir => per ogni entry => se file: addFile, se dir: ricorsione.
     *   Future.all() sulle entry fa il merge dei sotto-report, l'ultima future che si completa è
     *   quella relativa al Future.all della directory radice
     *
     * Le chiamate I/O (readDir, props) sono non bloccanti: ritornano subito la future.
     * Tutte le entry di una directory vengono elaborate in parallelo.
     */
    private Future<FSReport> scanDirectory(FileSystem fs, String dir) {
        return fs.readDir(dir)
                // Se la directory non è accessibile, la salto restituendo
                // una lista vuota: l'errore viene assorbito localmente
                // senza propagarsi all'intera scansione.
                .recover(err -> { // è tipo il .compose ma per gli errori.
                    log("WARNING: skipping inaccessible directory: " + dir);
                    return Future.succeededFuture(List.of());
                    //succededFuture serve per wrappare un valore in una future
                })
                .compose(entries -> {  // Uso compose per evitare il "callback hell", entries è la future
                    // completata dalla chiamata asincrona precedente, quindi a seguito del completamento della
                    // lettura di una directory, guardo tutte le entries e asincronicamente vengono processate tramite
                    // processEntry, compose deve comunque restituire sempre una future.
                    // Ogni entry (file o sotto-directory) produce una Future.
                    // Le entry nelle directory escluse vengono filtrate prima di tutto.
                    List<Future<FSReport>> futures = new ArrayList<>();

                    for (String entry : entries) {
                        if (!excludedDirs.contains(new File(entry).getName())) {
                            futures.add(processEntry(fs, entry)); // è la chiamata asincrona che da il via alla generazione
                            // di tutti i sotto-alberi, ogni volta che si incontra una directory viene generato un nuovo
                            // sotto-albero, che verranno alla fine "assemblati" bottom-up.
                        }
                    }

                    return Future.all(futures).map(ignored -> { // Qui tutto quello che si poteva delegare
                        // ai background thread è stato delegato. Future.all non blocca restituisce anche essa una future
                        // la cui callback associata viene eseguita dall'l'event-loop quando tutte le future saranno
                        // completate, quindi chiamare f.result qui dentro è sicuro perchè tutte le future sono in stato
                        // "succeeded".
                        FSReport report = new FSReport(nb, maxFS);
                        futures.forEach(f -> report.merge(f.result())); // .result è tipo un "unwrapper",
                        // da future a risultato effettivo.
                        return report;
                    });
                });
    }

    /**
     * Processa una singola entry del filesystem:
     * - Se è un file regolare, crea un FSReport con quel solo file.
     * - Se è una directory, ricorsione tramite scanDirectory.
     * - Se non è accessibile, restituisce un FSReport vuoto (errore assorbito).
     */
    private Future<FSReport> processEntry(FileSystem fs, String entry) {
        return fs.props(entry)
                .recover(err -> {
                    log("WARNING: skipping inaccessible entry: " + entry);
                    return Future.succeededFuture(null); // null = entry non accessibile
                })
                .compose(props -> {
                    if (props == null) {
                        return Future.succeededFuture(new FSReport(nb, maxFS));
                    }
                    if (props.isRegularFile()) {
                        FSReport r = new FSReport(nb, maxFS);
                        r.addFile(props.size());
                        return Future.succeededFuture(r); // Wrappo anche qui l'FSReport in una future essendo
                        // dentro la lambda passata a .compose
                    } else {
                        // È una directory: ricorsione
                        return scanDirectory(fs, entry); // scanDirectory ritorna già una
                        // future, non c'è bisogno di nessun wrap.
                    }
                });
    }

    private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}