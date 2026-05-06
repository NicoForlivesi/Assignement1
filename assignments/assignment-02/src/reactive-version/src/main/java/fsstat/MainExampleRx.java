package fsstat;

import java.util.Set;

public class MainExampleRx {

    public static void main(String[] args) throws InterruptedException {

        String targetDir = "D:\\Programmi (x86)\\ Steam";
        Set<String> excluded = Set.of("logs");
        long maxFS = 1_000_000L;
        int nb = 5;

        System.out.println("=== FSStatLibRx - Reactive Version ===");
        System.out.printf("Directory: %s%nMaxFS: %,d bytes%nBands: %d%n%n",
                targetDir, maxFS, nb);

        FSStatLibRx lib = new FSStatLibRx();

        long start = System.currentTimeMillis();

        lib.getFSReport(targetDir, maxFS, nb, excluded)
                .blockingSubscribe( // <- Importante blockingSubscribe e non una semplice subscribe
                        report -> {
                            long elapsed = System.currentTimeMillis() - start;
                            System.out.println(report);
                            System.out.printf("Completed in %d ms%n", elapsed);
                        },
                        err -> {
                            System.err.println("Error: " + err.getMessage());
                        }
                );
        // Thread.sleep(...); Sarebbe un altra soluzione se non si vuole usare .blockingSubscribe per imporre al main thread
        // di aspettare i worker prima di terminare, è però meno elegante.
        // Se non si usa ne blockingSubscribe ne Thread.sleep sul main, il main thread termina prima che venga prodotto
        // il risultato della scansione.
    }
}
