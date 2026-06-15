package fsstat;

/**
 * Report sulle statistiche del filesystem.
 * Oggetto mutabile: è sicuro perché viene aggiornato SOLO dall'event-loop
 * di Vert.x, che è single-thread, quindi nessuna race condition possibile.
 */
public class FSReportExt {

    private long totalFiles;
    private final long[] bands;
    private long overflow;
    private final long maxFS;
    private final int nb;
    private final long step;

    public FSReportExt(int nb, long maxFS) {
        this.nb = nb;
        this.maxFS = maxFS;
        this.step = (nb > 0) ? maxFS / nb : 1;
        this.bands = new long[nb];
    }

    /** Registra un file di dimensione size. */
    public void addFile(long size) {
        totalFiles++;
        if (size > maxFS) {
            overflow++;
        } else {
            int idx = (step == 0) ? 0 : (int) Math.min(size / step, nb - 1);
            bands[idx]++;
        }
    }

    /** Unisce i risultati di un sotto-albero in questo report. */
    public void merge(FSReportExt other) {
        this.totalFiles += other.totalFiles;
        this.overflow += other.overflow;
        for (int i = 0; i < nb; i++) {
            this.bands[i] += other.bands[i];
        }
    }

    /**
     * Usato dal timer periodico per passare alla GUI un'istanza immutabile
     * del report corrente.
     */
    public FSReportExt snapshot() {
        FSReportExt copy = new FSReportExt(nb, maxFS);
        copy.totalFiles = this.totalFiles;
        copy.overflow = this.overflow;
        System.arraycopy(this.bands, 0, copy.bands, 0, nb);
        return copy;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=== FSReport ===\n");
        sb.append("Total files : ").append(totalFiles).append("\n");
        for (int i = 0; i < nb; i++) {
            sb.append(String.format("  [%,d - %,d) : %d files%n", i * step, (i + 1) * step, bands[i]));
        }
        sb.append(String.format("  > %,d : %d files%n", maxFS, overflow));
        return sb.toString();
    }
}