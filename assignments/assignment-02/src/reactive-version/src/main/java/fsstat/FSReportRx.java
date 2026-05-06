package fsstat;

/**
 * Report sulle statistiche del filesystem.
 * Stesso report usato anche per la versione con event-loop
 */
public class FSReportRx {

    private long totalFiles;
    private long[] bands;
    private long overflow;
    private final long maxFS;
    private final int nb;
    private final long step;

    public FSReportRx(int nb, long maxFS) {
        this(nb, maxFS, 0, new long[nb], 0);
    }

    private FSReportRx(int nb, long maxFS, long totalFiles, long[] bands, long overflow) {
        this.nb = nb;
        this.maxFS = maxFS;
        this.totalFiles = totalFiles;
        this.bands = bands;
        this.overflow = overflow;
        this.step = maxFS / nb;
    }

    /**
     * Restituisce un nuovo report aggiornato con un file della dimensione data.
     * Nessuna mutazione: crea una nuova istanza.
     */
    public FSReportRx withFile(long size) {
        long newTotal = totalFiles + 1;
        long newOverflow = overflow;
        long[] newBands = bands.clone();

        if (size > maxFS) {
            newOverflow++;
        } else {
            int idx = (step == 0) ? 0 : (int) Math.min(size / step, nb - 1);
            newBands[idx]++;
        }

        return new FSReportRx(nb, maxFS, newTotal, newBands, newOverflow);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FSReport ===\n");
        sb.append("Total files : ").append(totalFiles).append("\n");
        for (int i = 0; i < nb; i++) {
            sb.append(String.format("  [%,d - %,d) : %d files%n", i * step, (i + 1) * step, bands[i]));
        }
        sb.append(String.format("  > %,d : %d files%n", maxFS, overflow));
        return sb.toString();
    }
}