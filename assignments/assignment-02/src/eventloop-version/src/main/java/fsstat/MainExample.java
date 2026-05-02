package fsstat;

import java.util.Set;

/**
 * Programma di esempio che mostra l'uso di FSStatLib.
 *
 * Si crea la libreria, si chiama il metodo asincrono, si aggancia la callback.
 * Il main thread NON si blocca ad aspettare — la callback è eseguita dall'event-loop.
 */
public class MainExample {

    public static void main(String[] args) {

        String targetDir = "D:\\Programmi (x86)\\ Steam";
        Set<String> excluded = Set.of("logs"); // Directory da escludere dalla scansione
        long maxFS = 1_000_000L;
        int nb = 5;

        System.out.println("=== FSStatLib v1 — Event-loop (Vert.x) ===");
        System.out.printf("Directory : %s%nMaxFS     : %,d bytes%nBands (NB): %d%n%n",
                targetDir, maxFS, nb);

        FSStatLib lib = new FSStatLib();
        long start = System.currentTimeMillis();

        lib.getFSReport(targetDir, maxFS, nb, excluded) // Chiamata asincrona: NON blocca, restituisce subito una Future
                .onSuccess(report -> {
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println(report);
                    System.out.printf("Completed in %d ms%n", elapsed);
                    lib.close();
                })
                .onFailure(err -> {
                    System.err.println("Error: " + err.getMessage());
                    lib.close();
                });

        System.out.println("[main] getFSReport called, waiting for event-loop to finish...");
        // Viene mantenuta la JVM viva finché ci sono verticle attivi.
    }
}