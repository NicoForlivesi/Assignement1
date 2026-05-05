package fsstat;

import java.util.Set;

public class MainExampleRx {

    public static void main(String[] args) {

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
                .subscribe(
                        report -> {
                            long elapsed = System.currentTimeMillis() - start;
                            System.out.println(report);
                            System.out.printf("Completed in %d ms%n", elapsed);
                        },
                        err -> {
                            System.err.println("Error: " + err.getMessage());
                        }
                );

        System.out.println("[main] getFSReport called, waiting for Rx pipeline...");
    }
}
