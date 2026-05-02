package fsstat;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Report parziale aggiornato durante la scansione.
 * Mutabile e acceduto solo dall'event-loop (nessuna race condition).
 * Espone toJson() per la pubblicazione sull'Event Bus.
 */
public class FSReportPartial {

    private long totalFiles;
    private long[] bands;
    private long overflow;
    private final long maxFS;
    private final int nb;
    private final long step;

    public FSReportPartial(int nb, long maxFS) {
        this.nb = nb;
        this.maxFS = maxFS;
        this.step = maxFS / nb;
        this.bands = new long[nb];
        this.totalFiles = 0;
        this.overflow = 0;
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

    public void merge(FSReportPartial other) {
        this.totalFiles += other.totalFiles;
        this.overflow += other.overflow;
        for (int i = 0; i < nb; i++) this.bands[i] += other.bands[i];
    }

    /** Serializza per la pubblicazione sull'Event Bus, per comodità uso formato Json */
    public JsonObject toJson() {
        JsonArray bandsArray = new JsonArray();
        for (long b : bands) bandsArray.add(b);
        return new JsonObject()
                .put("totalFiles", totalFiles)
                .put("overflow", overflow)
                .put("maxFS", maxFS)
                .put("nb", nb)
                .put("step", step)
                .put("bands", bandsArray);
    }

    /** Ricostruisce un FSReportPartial da un JsonObject ricevuto dall'Event Bus */
    public static FSReportPartial fromJson(JsonObject json) {
        int nb = json.getInteger("nb");
        long maxFS = json.getLong("maxFS");
        FSReportPartial r = new FSReportPartial(nb, maxFS);
        r.totalFiles = json.getLong("totalFiles");
        r.overflow = json.getLong("overflow");
        JsonArray arr = json.getJsonArray("bands");
        for (int i = 0; i < nb; i++) r.bands[i] = arr.getLong(i);
        return r;
    }

    public long getTotalFiles() { return totalFiles; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Files scanned: ").append(totalFiles).append("\n");
        for (int i = 0; i < nb; i++) {
            sb.append(String.format("  [%,d - %,d) : %d%n", i * step, (i + 1) * step, bands[i]));
        }
        sb.append(String.format("  > %,d : %d%n", maxFS, overflow));
        return sb.toString();
    }
}