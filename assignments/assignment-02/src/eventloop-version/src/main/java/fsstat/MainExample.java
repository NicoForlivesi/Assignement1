package fsstat;

import java.util.Set;

public class MainExample {

    public static void main(String[] args) {

        String targetDir = "D:\\Programmi (x86)\\ Steam";
        Set<String> excluded = Set.of("logs"); // Directory da escludere dalla scansione
        long maxFS = 1_000_000L;
        int nb = 5;

        System.out.println("=== FSStatLib — Event-loop Version ===");
        System.out.printf("Directory: %s%nMaxFS: %,d bytes%nBands: %d%n%n",
                targetDir, maxFS, nb);

        FSStatLib lib = new FSStatLib();
        long start = System.currentTimeMillis();

        var future = lib.getFSReport(targetDir, maxFS, nb, excluded); // Chiamata asincrona: NON blocca, restituisce subito una Future

        future.onSuccess(report -> {
            long elapsed = System.currentTimeMillis() - start;
            System.out.println(report);
            System.out.printf("Completed in %d ms%n", elapsed);
            lib.close();
        });
        future.onFailure(err -> {
            System.err.println("Error: " + err.getMessage());
            lib.close();
        });
        System.out.println("[main] getFSReport called, waiting for event-loop to finish...");
        // Viene mantenuta la JVM viva finchè la future è in stato "pending".
    }
}