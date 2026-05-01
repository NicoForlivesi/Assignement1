package fsstat;

/**
 * Report sulle statistiche del filesystem.
 * Oggetto mutabile: è sicuro perché viene aggiornato SOLO dall'event-loop
 * (VerticleBase garantisce esecuzione single-threaded delle callback).
 */
public class FSReport {

    private long totalFiles;
    private long[] bands;
    private long overflow;
    private final long maxFS;
    private final int nb;
    private final long step;

    public FSReport(int nb, long maxFS) {
        this.nb = nb;
        this.maxFS = maxFS;
        this.step = maxFS / nb;
        this.bands = new long[nb];
    }

    public void addFile(long size) {
        totalFiles++;
        if (size > maxFS) {
            overflow++;
        } else {
            int idx = (step == 0) ? 0 : (int) Math.min(size / step, nb - 1);
            bands[idx]++;
        }
    }

    public void merge(FSReport other) {
        this.totalFiles += other.totalFiles;
        this.overflow += other.overflow;
        for (int i = 0; i < nb; i++) {
            this.bands[i] += other.bands[i];
        }
    }

    public long getTotalFiles() { return totalFiles; }

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