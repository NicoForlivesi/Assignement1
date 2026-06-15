package fsstat;

import java.util.Set;

public class MainExampleVT {

    public static void main(String[] args) {

        String targetDir = "D:\\Programmi (x86)\\ Steam";
        Set<String> excluded = Set.of("logs");
        long maxFS = 1_000_000L;
        int nb = 5;

        System.out.println("FSStatLibVT (Virtual Threads Version)");
        System.out.printf("Directory: %s%nMaxFS: %,d bytes%nBands: %d%n%n",
                targetDir, maxFS, nb);

        FSStatLibVT lib = new FSStatLibVT();

        long start = System.currentTimeMillis();

        FSReportVT report = lib.getFSReport(targetDir, maxFS, nb, excluded);

        long elapsed = System.currentTimeMillis() - start;

        System.out.println(report);
        System.out.printf("Completed in %d ms%n", elapsed);
    }
}
