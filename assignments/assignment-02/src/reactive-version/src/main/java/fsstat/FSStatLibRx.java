package fsstat;

import io.reactivex.rxjava3.core.Flowable;

import java.io.File;
import java.util.Set;

public class FSStatLibRx {

    /**
     * Scansione ricorsiva: restituisce un Flowable che emette TUTTI i file
     * presenti nella directory (e sotto-directory), esclusi quelli filtrati.
     */
    private Flowable<File> scanDir(File dir, Set<String> excluded) {
        return Flowable.defer(() -> {
            File[] entries = dir.listFiles();
            if (entries == null) {
                return Flowable.empty();
            }

            return Flowable.fromArray(entries)
                    .flatMap(entry -> {
                        if (entry.isDirectory()) {
                            if (excluded.contains(entry.getName())) {
                                return Flowable.empty();
                            }
                            return scanDir(entry, excluded);
                        } else {
                            return Flowable.just(entry);
                        }
                    });
        });
    }

    /**
     * Versione Rx della libreria: restituisce un Flowable che emette UN SOLO FSReport finale.
     */
    public Flowable<FSReportRx> getFSReport(String dir, long maxFS, int nb, Set<String> excluded) {

        return scanDir(new File(dir), excluded)
                .map(file -> file.length())
                .reduce(new FSReportRx(nb, maxFS), (report, size) -> {
                    report.addFile(size);
                    return report;
                })
                .toFlowable(); // convertiamo Single<FSReportRx> → Flowable<FSReportRx>
    }
}
