package fsstat;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.util.Set;

/**
 * Versione Rx della libreria FSStatLib.
 * Usa flussi reattivi per scansionare ricorsivamente il filesystem
 * e produce un singolo FSReportRx finale tramite reduce().
 */
public class FSStatLibRx {

    /**
     * Restituisce un Flowable che emette TUTTI i file presenti nella directory
     * (e sotto-directory), esclusi quelli filtrati.
     * - È un flusso "cold": la scansione parte solo quando qualcuno chiama .subscribe().
     * - Uso il metodo .defer() per evitare che la scansione parta subito.
     * - Usiamo uno scheduler per la parallelizzazione della lettura delle directory.
     */
    private Flowable<File> scanDir(File dir, Set<String> excluded) {
        return Flowable.defer(() -> // .defer serve a rimandare l'esecuzione della lambda fino al momento
            // in cui non si chiama .subscribe
                Flowable.fromCallable(() -> dir.listFiles())
                        .subscribeOn(Schedulers.io()) // sposta la lettura della directory sul pool I/O di RxJava, che
                        // viene gestito dinamicamente dal framework, flatMap sottoscrive i sotto-flussi ricorsivi in modo
                        // concorrente, ottenendo la parallelizzazione della visita..
                        .flatMap(entries -> {
                            if (entries == null) {
                                return Flowable.empty(); // directory non leggibile
                            }
                            return Flowable.fromArray(entries)
                                    .flatMap(entry -> {
                                        if (entry.isDirectory()) {
                                            if (excluded.contains(entry.getName())) {
                                                return Flowable.empty();
                                            }
                                            return scanDir(entry, excluded); // ricorsione, caso directory
                                        }
                                        return Flowable.just(entry); // caso file
                                    });
                        })
        );
    }

    /**
     * Metodo principale dell’assignement:
     * restituisce un Flowable che emette UN SOLO FSReportRx finale.
     */
    public Flowable<FSReportRx> getFSReport(String dir, long maxFS, int nb, Set<String> excluded) {
        return scanDir(new File(dir), excluded)
                .map(file -> file.length()) // trasformiamo ogni file in una dimensione
                .reduce(
                        new FSReportRx(nb, maxFS),
                        (report, size) -> report.withFile(size)
                ) // reduce è tipo un fold su scala
                .toFlowable();
    }
}
