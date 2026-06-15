package fsstat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class FSStatLibVT {

    /**
     * Scansione ricorsiva parallela usando Virtual Threads.
     * Ogni directory viene scansionata in un virtual thread dedicato.
     * newVirtualThreadPerTaskExecutor crea un executor che non riusa i thread, ogni submit() crea un nuovo virtual thread.
     */
    public FSReportVT getFSReport(String dir, long maxFS, int nb, Set<String> excluded) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            /**
             * Qui parte la ricorsione. scanDir() ritorna un FSReportVT finale. L'executor rimane "aperto" per tutta la
             * durata della ricorsione, così ogni sotto-directory può creare nuovi virtual threads.
             */
            return scanDir(new File(dir), excluded, maxFS, nb, executor);
        }
    }

    /**
     * Ritorna un FSReportVT che rappresenta tutti i file sotto questa directory.
     */
    private FSReportVT scanDir(File dir, Set<String> excluded, long maxFS, int nb, ExecutorService executor) {
        File[] entries = dir.listFiles();
        if (entries == null) { // Se directory non leggibile ritorna report vuoto
            return new FSReportVT(nb, maxFS);
        }

        // FSReportVT è immutabile: withFile e merge non modificano l'oggetto
        // ma restituiscono sempre una nuova istanza aggiornata.
        FSReportVT report = new FSReportVT(nb, maxFS);
        // Ogni volta che troviamo una sotto-directory, lanciamo un virtual thread che esegue scanDir() su quella directory.
        // submit() ritorna una Future<FSReportVT>, quindi raccogliamo tutti i Future.
        List<Future<FSReportVT>> futures = new ArrayList<>(); // Importante: questa è la Future di java.util.concurrent,
        // non c'entra niente con quella usata nella versione con vertx, questa è bloccante attraverso .get che chiamiamo dopo.

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (excluded.contains(entry.getName())) {
                    continue;
                }
                // Ogni directory un nuovo virtual thread
                futures.add(executor.submit(() ->
                        scanDir(entry, excluded, maxFS, nb, executor)
                ));
            } else {
                report = report.withFile(entry.length()); // Caso file aggiorno il report locale
            }
        }

        /**
         * Combiniamo i risultati delle sotto-directory con f.get che:
         * -aspetta che il virtual thread abbia finito
         * -ritorna il FSReportVT della sotto-directory
         * poi report.merge:
         * -unisce il report locale con quello della sotto-directory
         * -crea un nuovo report immutabile
         */
        for (Future<FSReportVT> f : futures) {
            try {
                report = report.merge(f.get()); // Qui blocchiamo finchè le future non sono completate
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return report;
    }
}
