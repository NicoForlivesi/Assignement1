package fsstat;

public class FSReport {
    private final long totalFiles;
    private final long[] distribution;

    public FSReport(long totalFiles, long[] distribution) {
        this.totalFiles = totalFiles;
        this.distribution = distribution;
    }
}
